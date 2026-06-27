@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
package com.rld.justlisten.media

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.cancel
import platform.CoreFoundation.CFRelease
import com.rld.justlisten.viewmodel.interfaces.Item
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import co.touchlab.kermit.Logger
import platform.AVFoundation.*
import platform.AVFAudio.*
import platform.Foundation.*
import platform.MediaPlayer.*
import platform.SystemConfiguration.*
import platform.posix.sockaddr_in
import platform.posix.AF_INET
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import platform.CoreMedia.CMTimeMake
import platform.CoreMedia.CMTimeGetSeconds
import kotlinx.cinterop.*
import platform.UIKit.UIImage
import platform.MediaToolbox.*
import platform.AudioToolbox.*
import kotlin.math.pow

class IOSMusicPlayer(
    private val favoritesRepository: FavoritesRepository,
    private val settingsRepository: com.rld.justlisten.datalayer.repositories.SettingsRepository,
    private val settingsViewModel: com.rld.justlisten.viewmodel.settings.SettingsViewModel
) : MusicPlayer {
    private val player1 = AVQueuePlayer()
    private val player2 = AVQueuePlayer()
    private var currentPlayer = player1
    private var secondaryPlayer = player2
    private var isCrossfading = false
    private var crossfadeJob: kotlinx.coroutines.Job? = null
    private var timeObserverToken2: Any? = null

    private fun cancelCrossfade(pauseActivePlayer: Boolean = false) {
        crossfadeJob?.cancel()
        crossfadeJob = null
        
        if (isCrossfading) {
            // Restore default volumes
            player1.volume = userVolume
            player2.volume = userVolume
            
            // Stop and clear the fading-out player
            secondaryPlayer.pause()
            secondaryPlayer.removeAllItems()
            
            if (pauseActivePlayer) {
                currentPlayer.pause()
            }
            
            isCrossfading = false
        }
    }

    private var activePlayerItem: AVPlayerItem? = null
    private var preloadedPlayerItem: AVPlayerItem? = null
    private var preloadedSongId: String? = null
    private var preloadJob: kotlinx.coroutines.Job? = null
    private var playJob: kotlinx.coroutines.Job? = null
    
    private var playlistItems = mutableListOf<MediaMetadata>()
    private var currentIndex = -1
    private var lastNowPlayingUpdateMs = 0L
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var shuffledIndices = listOf<Int>()
    private var isShuffleEnabled = false
    private var repeatMode = RepeatMode.NONE
    private var favoriteIdsSet = emptySet<String>()

    private val eqLock = kotlinx.atomicfu.locks.SynchronizedObject()
    private val eqStorage: CPointer<FloatVar> by lazy {
        nativeHeap.allocArray<FloatVar>(74).apply {
            this[0] = 0.0f
            this[71] = 0.0f
            this[72] = 1.0f
            this[73] = 0.0f
        }
    }
    // Separate EQ filter state for the secondary player to prevent data races
    // during crossfade when both players' audio taps run concurrently.
    private val eqStorage2: CPointer<FloatVar> by lazy {
        nativeHeap.allocArray<FloatVar>(74).apply {
            this[0] = 0.0f
            this[71] = 0.0f
            this[72] = 1.0f
            this[73] = 0.0f
        }
    }
    
    private var lastLoadedArtworkUrl: String? = null
    private var lastLoadedArtwork: UIImage? = null
    private var timeObserverToken: Any? = null
    
    private val activeDownloads = mutableMapOf<String, NSURLSessionDownloadTask>()
    
    private var interruptionObserver: Any? = null
    private var routeChangeObserver: Any? = null
    private var playToEndObserver: Any? = null
    private val artworkCache = mutableMapOf<String, UIImage>()


    private var userVolume: Float = 1.0f

    // Public property to allow volume fading from Sleep Timer
    var volume: Float
        get() = userVolume
        set(value) {
            userVolume = value
            if (!isCrossfading) {
                currentPlayer.volume = value
            }
        }

    init {
        scope.launch {
            settingsViewModel.settingsState.collect { state ->
                kotlinx.atomicfu.locks.synchronized(eqLock) {
                    val eqEnabled = if (state.isEqEnabled) 1.0f else 0.0f
                    val normEnabled = if (state.isVolumeNormalizationEnabled) 1.0f else 0.0f
                    // Update both storages so both players use current EQ settings
                    for (storage in arrayOf(eqStorage, eqStorage2)) {
                        storage[0] = eqEnabled
                        for (i in 0 until 5) {
                            val bandOffset = 1 + i * 14
                            if (i < state.eqBands.size) {
                                storage[bandOffset + 0] = state.eqBands[i]
                            }
                        }
                        storage[72] = 1.0f
                        storage[73] = normEnabled
                    }
                }
            }
        }

        try {
            val audioSession = AVAudioSession.sharedInstance()
            memScoped {
                val categoryError = alloc<ObjCObjectVar<NSError?>>() 
                audioSession.setCategory(AVAudioSessionCategoryPlayback, error = categoryError.ptr)
                categoryError.value?.let { Logger.e { "Audio setCategory failed: ${it.localizedDescription}" } }
                
                val activeError = alloc<ObjCObjectVar<NSError?>>()
                audioSession.setActive(true, error = activeError.ptr)
                activeError.value?.let { Logger.e { "Audio setActive failed: ${it.localizedDescription}" } }
            }
        } catch (e: Exception) {
            Logger.e(e) { "Audio session setup error" }
        }

        // Register periodic time observer for progress updates
        val interval = CMTimeMake(250, 1000)
        timeObserverToken = player1.addPeriodicTimeObserverForInterval(
            interval = interval,
            queue = dispatch_get_main_queue(),
            usingBlock = { _ ->
                if (currentPlayer == player1) updateProgress()
            }
        )
        timeObserverToken2 = player2.addPeriodicTimeObserverForInterval(
            interval = interval,
            queue = dispatch_get_main_queue(),
            usingBlock = { _ ->
                if (currentPlayer == player2) updateProgress()
            }
        )

        // Periodically monitor network at a relaxed interval (5s)
        scope.launch(Dispatchers.Main) {
            while (isActive) {
                monitorNetwork()
                delay(5000L)
            }
        }

        // Collect favorite tracks list changes to update metadata dynamically
        scope.launch {
            favoritesRepository.getFavoritePlaylistFlow().collect { favoriteList ->
                val ids = favoriteList.map { it.id }.toSet()
                favoriteIdsSet = ids
                
                // Update current media favorite status
                val currentMedia = _playbackState.value.currentMedia
                if (currentMedia != null) {
                    val isFav = ids.contains(currentMedia.id)
                    if (isFav != currentMedia.isFavorite) {
                        _playbackState.update { state ->
                            state.copy(
                                currentMedia = state.currentMedia?.copy(isFavorite = isFav)
                            )
                        }
                    }
                }

                // Update current playlist items favorite status
                playlistItems = playlistItems.map { item ->
                    item.copy(isFavorite = ids.contains(item.id))
                }.toMutableList()
                _currentPlaylist.value = playlistItems
            }
        }

        setupAudioObservers()
        setupRemoteCommandCenter()
    }

    override var currentlyPlayingPlaylistId: String? = null

    private val _playbackState = MutableStateFlow(
        PlaybackState(PlaybackStatus.IDLE, 0L, null, false, RepeatMode.NONE)
    )
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<MediaMetadata>>(emptyList())
    override val currentPlaylist: StateFlow<List<MediaMetadata>> = _currentPlaylist.asStateFlow()

    private val _isConnected = MutableStateFlow(true)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _networkError = MutableStateFlow(false)
    override val networkError: StateFlow<Boolean> = _networkError.asStateFlow()

    override fun play() {
        currentPlayer.play()
        updateState(PlaybackStatus.PLAYING)
        updateNowPlayingInfo(_playbackState.value.currentMedia, _playbackState.value.currentPosition)
    }

    override fun pause() {
        if (isCrossfading) {
            cancelCrossfade(pauseActivePlayer = true)
        } else {
            currentPlayer.pause()
        }
        updateState(PlaybackStatus.PAUSED)
        updateNowPlayingInfo(_playbackState.value.currentMedia, _playbackState.value.currentPosition)
    }

    override fun stop() {
        cancelCrossfade()
        currentPlayer.pause()
        currentPlayer.removeAllItems()
        secondaryPlayer.pause()
        secondaryPlayer.removeAllItems()
        activePlayerItem = null
        invalidatePreload()
        updateState(PlaybackStatus.STOPPED)
        updateNowPlayingInfo(null, 0L)
    }

    override fun playMedia(mediaId: String) {
        val index = playlistItems.indexOfFirst { it.id == mediaId }
        if (index != -1) {
            currentIndex = index
            playTrack(playlistItems[index])
        }
    }

    override fun playMedia(mediaId: String, playlist: List<Item>) {
        if (playlistItems.size != playlist.size || playlistItems.zip(playlist).any { it.first.id != it.second.id }) {
            val mappedList = playlist.map {
                MediaMetadata(
                    id = it.id,
                    title = it.title,
                    artist = it.user,
                    duration = it.duration.toLong() * 1000L,
                    artworkUrl = it.songIconList.songImageURL480px,
                    lowResArtworkUrl = it.songIconList.songImageURL150px,
                    isFavorite = favoriteIdsSet.contains(it.id),
                    repostCount = it.repostCount,
                    favoriteCount = it.favoriteCount,
                    commentCount = it.commentCount,
                    playCount = it.playCount,
                    artistId = it.userId
                )
            }.toMutableList()
    
            playlistItems = mappedList
            _currentPlaylist.value = mappedList
    
            if (isShuffleEnabled) {
                shuffledIndices = playlistItems.indices.shuffled()
            }
        }

        val index = playlistItems.indexOfFirst { it.id == mediaId }
        if (index != -1) {
            currentIndex = index
            playTrack(playlistItems[index])
        }
    }

    private fun getCacheFileUrl(songId: String): NSURL? {
        val fileManager = NSFileManager.defaultManager
        val cacheUrls = fileManager.URLsForDirectory(NSCachesDirectory, NSUserDomainMask)
        val cacheDirectory = cacheUrls.firstOrNull() as? NSURL ?: return null
        return cacheDirectory.URLByAppendingPathComponent("music_$songId.mp3")
    }

    private fun isSongCached(songId: String): Boolean {
        val url = getCacheFileUrl(songId) ?: return false
        val path = url.path ?: return false
        return NSFileManager.defaultManager.fileExistsAtPath(path)
    }

    private fun triggerBackgroundDownload(songId: String, nsUrl: NSURL) {
        if (isSongCached(songId) || activeDownloads.containsKey(songId)) {
            return
        }
        
        val localUrl = getCacheFileUrl(songId) ?: return
        
        val task = NSURLSession.sharedSession.downloadTaskWithURL(nsUrl) { location, _, error ->
            dispatch_async(dispatch_get_main_queue()) {
                activeDownloads.remove(songId)
                if (error == null && location != null) {
                    val fileManager = NSFileManager.defaultManager
                    try {
                        val path = localUrl.path
                        if (path != null) {
                            if (fileManager.fileExistsAtPath(path)) {
                                fileManager.removeItemAtURL(localUrl, error = null)
                            }
                            fileManager.moveItemAtURL(location, toURL = localUrl, error = null)
                            scope.launch(Dispatchers.Default) {
                                cleanCache()
                            }
                        }
                    } catch (e: Exception) {
                        Logger.e(e) { "Failed to save downloaded song $songId" }
                    }
                }
            }
        }
        activeDownloads[songId] = task
        task.resume()
    }

    private fun cleanCache() {
        val fileManager = NSFileManager.defaultManager
        val cacheUrls = fileManager.URLsForDirectory(NSCachesDirectory, NSUserDomainMask)
        val cacheDirectory = cacheUrls.firstOrNull() as? NSURL ?: return
        
        val files = fileManager.contentsOfDirectoryAtURL(
            cacheDirectory,
            includingPropertiesForKeys = null,
            options = 0UL,
            error = null
        )?.mapNotNull { it as? NSURL } ?: return
        
        val cachedMusicFiles = files.filter { it.lastPathComponent?.startsWith("music_") == true && it.lastPathComponent?.endsWith(".mp3") == true }
        
        val fileInfos = cachedMusicFiles.mapNotNull { url ->
            val path = url.path ?: return@mapNotNull null
            val attributes = fileManager.attributesOfItemAtPath(path, error = null) ?: return@mapNotNull null
            val size = (attributes[NSFileSize] as? NSNumber)?.longLongValue() ?: 0L
            val modDate = (attributes[NSFileModificationDate] as? NSDate)?.timeIntervalSince1970 ?: 0.0
            Triple(url, size, modDate)
        }
        
        val totalSize = fileInfos.sumOf { it.second }
        val limit = 150 * 1024 * 1024L // 150 MB
        
        if (totalSize > limit) {
            val sorted = fileInfos.sortedBy { it.third }
            var currentSize = totalSize
            for (info in sorted) {
                if (currentSize <= limit) break
                fileManager.removeItemAtURL(info.first, error = null)
                currentSize -= info.second
            }
        }
    }

    private suspend fun createPlayerItem(songId: String, forSecondaryPlayer: Boolean = false): AVPlayerItem? = kotlinx.coroutines.withContext(Dispatchers.Default) {
        val cachedUrl = getCacheFileUrl(songId)
        val playerItem = if (cachedUrl != null && cachedUrl.path != null && NSFileManager.defaultManager.fileExistsAtPath(cachedUrl.path!!)) {
            AVPlayerItem.playerItemWithURL(cachedUrl)
        } else {
            val baseUrl = com.rld.justlisten.datalayer.utils.Constants.BASEURL
            val appName = com.rld.justlisten.datalayer.utils.Constants.appName.replace(" ", "%20")
            val streamUrl = "$baseUrl/v1/tracks/$songId/stream?app_name=$appName"
            val nsUrl = NSURL.URLWithString(streamUrl)
            if (nsUrl != null) {
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    triggerBackgroundDownload(songId, nsUrl)
                }
                AVPlayerItem.playerItemWithURL(nsUrl)
            } else {
                null
            }
        }
        if (playerItem != null) {
            val (isEqEnabled, isNormEnabled) = kotlinx.coroutines.withContext(Dispatchers.Main) {
                Pair(
                    settingsRepository.getSettingsInfo().isEqEnabled,
                    settingsRepository.isVolumeNormalizationEnabled
                )
            }
            if (isEqEnabled || isNormEnabled) {
                val storage = if (forSecondaryPlayer) eqStorage2 else eqStorage
                configureAudioProcessingTap(playerItem, storage)
            }
        }
        playerItem
    }

    private fun configureAudioProcessingTap(playerItem: AVPlayerItem, storage: CPointer<FloatVar> = eqStorage) {
        memScoped {
            val callbacks = alloc<MTAudioProcessingTapCallbacks>()
            callbacks.version = kMTAudioProcessingTapCallbacksVersion_0
            callbacks.clientInfo = storage
            
            callbacks.init = staticCFunction { tap, clientInfo, tapStorageOut ->
                tapStorageOut?.pointed?.value = clientInfo
            }
            callbacks.finalize = staticCFunction { tap ->
                // Owned by player
            }
            callbacks.prepare = staticCFunction { tap, maxFrames, processingFormat ->
                val statePtr = MTAudioProcessingTapGetStorage(tap)?.reinterpret<FloatVar>()
                if (statePtr != null && processingFormat != null) {
                    val asbd = processingFormat.pointed
                    statePtr[71] = asbd.mSampleRate.toFloat()
                    statePtr[72] = 1.0f // request recalculation
                }
            }
            callbacks.unprepare = staticCFunction { tap ->
                // No-op
            }
            callbacks.process = staticCFunction { tap, numberFrames, flags, bufferListInOut, numberFramesOut, flagsOut ->
                val status = MTAudioProcessingTapGetSourceAudio(tap, numberFrames, bufferListInOut, flagsOut, null, numberFramesOut)
                if (status != 0 || bufferListInOut == null) {
                    return@staticCFunction
                }

                val statePtr = MTAudioProcessingTapGetStorage(tap)?.reinterpret<FloatVar>() ?: return@staticCFunction
                val framesToProcess = numberFramesOut?.pointed?.value ?: numberFrames

                if (statePtr[72] > 0.5f) {
                    computeCoefficients(statePtr)
                }

                val bufferList = bufferListInOut.pointed
                val numBuffers = bufferList.mNumberBuffers.toInt()
                
                for (b in 0 until numBuffers) {
                    val audioBuffer = bufferList.mBuffers[b]
                    val mData = audioBuffer.mData?.reinterpret<FloatVar>() ?: continue
                    val channels = audioBuffer.mNumberChannels.toInt()
                    val chIndex = if (b == 0) 0 else 1
                    processAudioSamples(statePtr, mData, framesToProcess.toInt(), channels, chIndex)
                }
            }

            val tapVar = alloc<MTAudioProcessingTapRefVar>()
            val status = MTAudioProcessingTapCreate(null, callbacks.ptr, kMTAudioProcessingTapCreationFlag_PreEffects, tapVar.ptr)
            if (status == 0) {
                val tap = tapVar.value
                val audioMix = AVMutableAudioMix.audioMix()
                val audioTracks = playerItem.asset.tracksWithMediaType(AVMediaTypeAudio)
                val inputParametersList = mutableListOf<AVMutableAudioMixInputParameters>()
                for (trackObj in audioTracks) {
                    val track = trackObj as? AVAssetTrack ?: continue
                    val inputParams = AVMutableAudioMixInputParameters.audioMixInputParametersWithTrack(track)
                    inputParams.setAudioTapProcessor(tap)
                    inputParametersList.add(inputParams)
                }
                audioMix.setInputParameters(inputParametersList)
                playerItem.audioMix = audioMix
            }
        }
    }

    private fun playTrack(metadata: MediaMetadata) {
        cancelCrossfade()
        playJob?.cancel()
        
        if (currentPlayer.items().size > 1 && preloadedSongId == metadata.id && preloadedPlayerItem != null) {
            currentPlayer.advanceToNextItem()
            val idx = playlistItems.indexOfFirst { it.id == metadata.id }
            if (idx != -1) {
                currentIndex = idx
            }
            activePlayerItem = preloadedPlayerItem
            updateState(PlaybackStatus.BUFFERING, metadata)
            lastNowPlayingUpdateMs = 0L
            updateNowPlayingInfo(metadata, 0L)
            
            preloadedPlayerItem = null
            preloadedSongId = null
            preloadNextTrack()
            return
        }

        updateState(PlaybackStatus.BUFFERING, metadata)
        lastNowPlayingUpdateMs = 0L
        updateNowPlayingInfo(metadata, 0L)

        playJob = scope.launch(Dispatchers.Main) {
            val playerItem = if (preloadedSongId == metadata.id) {
                preloadedPlayerItem
            } else {
                createPlayerItem(metadata.id)
            }
    
            if (playerItem != null && isActive) {
                activePlayerItem = playerItem
                currentPlayer.removeAllItems()
                currentPlayer.insertItem(playerItem, afterItem = null)
                currentPlayer.play()
                
                preloadedPlayerItem = null
                preloadedSongId = null
                preloadNextTrack()
            } else if (isActive) {
                updateState(PlaybackStatus.ERROR)
            }
        }
    }

    private fun invalidatePreload() {
        preloadJob?.cancel()
        preloadedPlayerItem?.let {
            player1.removeItem(it)
            player2.removeItem(it)
        }
        preloadedPlayerItem = null
        preloadedSongId = null
    }

    private fun preloadNextTrack() {
        invalidatePreload()
        
        val nextIndex = getNextTrackIndex()
        if (nextIndex == -1 || nextIndex >= playlistItems.size) {
            return
        }
        
        val nextMetadata = playlistItems[nextIndex]
        
        preloadJob = scope.launch(Dispatchers.Main) {
            delay(2000L)
            
            if (repeatMode == RepeatMode.ONE) {
                return@launch
            }
            
            val nextItem = createPlayerItem(nextMetadata.id)
            if (nextItem != null) {
                preloadedPlayerItem = nextItem
                preloadedSongId = nextMetadata.id
                
                val currentActiveItem = currentPlayer.currentItem
                if (currentActiveItem != null && currentPlayer.items().size == 1 && !settingsRepository.isCrossfadeEnabled) {
                    currentPlayer.insertItem(nextItem, afterItem = currentActiveItem)
                }
            }
        }
    }

    private fun getNextTrackIndex(): Int {
        return PlaybackQueueNavigator.getNextIndex(
            currentIndex = currentIndex,
            playlistSize = playlistItems.size,
            shuffledIndices = shuffledIndices,
            isShuffleEnabled = isShuffleEnabled,
            repeatMode = repeatMode
        )
    }

    private fun updateState(status: PlaybackStatus, currentMedia: MediaMetadata? = _playbackState.value.currentMedia) {
        _playbackState.update { state ->
            state.copy(
                status = status,
                currentMedia = currentMedia ?: state.currentMedia
            )
        }
    }

    override fun skipToNext() {
        cancelCrossfade()
        val nextIndex = getNextTrackIndex()
        if (nextIndex != -1) {
            val nextItem = playlistItems[nextIndex]
            if (currentPlayer.items().size > 1 && preloadedSongId == nextItem.id && preloadedPlayerItem != null) {
                currentPlayer.advanceToNextItem()
                currentIndex = nextIndex
                activePlayerItem = preloadedPlayerItem
                updateState(PlaybackStatus.BUFFERING, nextItem)
                updateNowPlayingInfo(nextItem, 0L)
                preloadedPlayerItem = null
                preloadedSongId = null
                preloadNextTrack()
            } else {
                currentIndex = nextIndex
                playTrack(playlistItems[currentIndex])
            }
        } else {
            stop()
        }
    }

    override fun skipToPrevious() {
        cancelCrossfade()
        val prevIndex = PlaybackQueueNavigator.getPreviousIndex(
            currentIndex = currentIndex,
            playlistSize = playlistItems.size,
            shuffledIndices = shuffledIndices,
            isShuffleEnabled = isShuffleEnabled,
            repeatMode = repeatMode
        )
        if (prevIndex != -1) {
            currentIndex = prevIndex
            playTrack(playlistItems[currentIndex])
        }
    }

    override fun seekTo(position: Long) {
        cancelCrossfade()
        val time = CMTimeMake(position, 1000)
        currentPlayer.seekToTime(time)
        updateNowPlayingInfo(_playbackState.value.currentMedia, position)
    }

    override fun updatePlaylist(list: List<Item>) {
        playlistItems = list.map {
            MediaMetadata(
                id = it.id,
                title = it.title,
                artist = it.user,
                duration = it.duration.toLong() * 1000L,
                artworkUrl = it.songIconList.songImageURL480px,
                lowResArtworkUrl = it.songIconList.songImageURL150px,
                isFavorite = favoriteIdsSet.contains(it.id),
                repostCount = it.repostCount,
                favoriteCount = it.favoriteCount,
                commentCount = it.commentCount,
                playCount = it.playCount,
                artistId = it.userId
            )
        }.toMutableList()
        _currentPlaylist.value = playlistItems
        
        // Regenerate shuffled indices matching new playlist size
        if (isShuffleEnabled) {
            shuffledIndices = playlistItems.indices.shuffled()
        }
        preloadNextTrack()
    }

    override fun addTracksToQueue(tracks: List<Item>) {
        val newMetadata = tracks.map {
            MediaMetadata(
                id = it.id,
                title = it.title,
                artist = it.user,
                duration = it.duration.toLong() * 1000L,
                artworkUrl = it.songIconList.songImageURL480px,
                lowResArtworkUrl = it.songIconList.songImageURL150px,
                isFavorite = favoriteIdsSet.contains(it.id),
                repostCount = it.repostCount,
                favoriteCount = it.favoriteCount,
                commentCount = it.commentCount,
                playCount = it.playCount,
                artistId = it.userId
            )
        }
        playlistItems.addAll(newMetadata)
        _currentPlaylist.value = playlistItems.toList()
        
        if (isShuffleEnabled) {
            shuffledIndices = playlistItems.indices.shuffled()
        }
        preloadNextTrack()
    }

    override fun setShuffleModeEnabled(enabled: Boolean) {
        isShuffleEnabled = enabled
        if (enabled) {
            shuffledIndices = playlistItems.indices.shuffled()
        }
        _playbackState.update { it.copy(isShuffleModeEnabled = enabled) }
        preloadNextTrack()
    }

    override fun setRepeatMode(repeatMode: RepeatMode) {
        this.repeatMode = repeatMode
        _playbackState.update { it.copy(repeatMode = repeatMode) }
        if (repeatMode == RepeatMode.ONE) {
            val items = currentPlayer.items()
            if (items.size > 1) {
                for (i in 1 until items.size) {
                    val item = items[i] as? AVPlayerItem
                    if (item != null) currentPlayer.removeItem(item)
                }
            }
        }
        preloadNextTrack()
    }

    override fun refreshMetadata() {
        _playbackState.update { state ->
            val currentMedia = state.currentMedia
            if (currentMedia != null) {
                val isFav = favoriteIdsSet.contains(currentMedia.id)
                state.copy(
                    currentMedia = currentMedia.copy(isFavorite = isFav)
                )
            } else {
                state
            }
        }
    }

    override fun updateTrackMetadata(
        songId: String,
        repostCount: Int,
        favoriteCount: Int,
        commentCount: Int,
        playCount: Int,
        artistId: String
    ) {
        val index = playlistItems.indexOfFirst { it.id == songId }
        if (index != -1) {
            val song = playlistItems[index]
            val updated = song.copy(
                repostCount = repostCount,
                favoriteCount = favoriteCount,
                commentCount = commentCount,
                playCount = playCount,
                artistId = artistId
            )
            playlistItems[index] = updated
            _currentPlaylist.value = playlistItems.toList()
            
            // Also update currentMedia in playbackState if it matches
            val currentMedia = _playbackState.value.currentMedia
            if (currentMedia != null && currentMedia.id == songId) {
                _playbackState.update { state ->
                    state.copy(
                        currentMedia = currentMedia.copy(
                            repostCount = repostCount,
                            favoriteCount = favoriteCount,
                            commentCount = commentCount,
                            playCount = playCount,
                            artistId = artistId
                        )
                    )
                }
                updateNowPlayingInfo(_playbackState.value.currentMedia, _playbackState.value.currentPosition)
            }
        }
    }

    override fun removeTrack(index: Int) {
        if (index in playlistItems.indices) {
            playlistItems.removeAt(index)
            _currentPlaylist.value = playlistItems.toList()
            if (isShuffleEnabled) {
                shuffledIndices = playlistItems.indices.shuffled()
            }
            preloadNextTrack()
        }
    }

    override fun moveTrack(fromIndex: Int, toIndex: Int) {
        if (fromIndex in playlistItems.indices && toIndex in playlistItems.indices) {
            val item = playlistItems.removeAt(fromIndex)
            playlistItems.add(toIndex, item)
            
            // Adjust currentIndex based on list movement
            if (currentIndex == fromIndex) {
                currentIndex = toIndex
            } else if (currentIndex in (fromIndex + 1)..toIndex) {
                currentIndex--
            } else if (currentIndex in toIndex until fromIndex) {
                currentIndex++
            }
            
            _currentPlaylist.value = playlistItems.toList()
            preloadNextTrack()
        }
    }

    private fun updateProgress() {
        val player = currentPlayer
        val currentItem = player.currentItem ?: return

        val durationTime = currentItem.duration
        val durationSec = CMTimeGetSeconds(durationTime)
        val durationMs = if (!durationSec.isNaN() && durationSec > 0.0) {
            (durationSec * 1000).toLong()
        } else {
            0L
        }

        val currentTime = player.currentTime()
        val currentSec = CMTimeGetSeconds(currentTime)
        val currentMs = if (!currentSec.isNaN() && currentSec > 0.0) {
            (currentSec * 1000).toLong()
        } else {
            0L
        }

        val currentStatus = when (player.timeControlStatus) {
            AVPlayerTimeControlStatusPlaying -> PlaybackStatus.PLAYING
            AVPlayerTimeControlStatusWaitingToPlayAtSpecifiedRate -> PlaybackStatus.BUFFERING
            AVPlayerTimeControlStatusPaused -> PlaybackStatus.PAUSED
            else -> PlaybackStatus.IDLE
        }

        val currentMedia = _playbackState.value.currentMedia
        val oldDuration = currentMedia?.duration ?: 0L
        val oldStatus = _playbackState.value.status

        val updatedMedia = if (currentMedia?.duration != durationMs) {
            currentMedia?.copy(duration = durationMs)
        } else {
            currentMedia
        }

        _playbackState.update { state ->
            state.copy(
                status = currentStatus,
                currentPosition = currentMs,
                currentMedia = updatedMedia
            )
        }

        val driftThresholdMs = 10000L
        val shouldUpdateNowPlaying = updatedMedia != null && (
            durationMs != oldDuration || 
            currentStatus != oldStatus || 
            (currentMs - lastNowPlayingUpdateMs) > driftThresholdMs ||
            lastNowPlayingUpdateMs == 0L
        )
        if (shouldUpdateNowPlaying) {
            updateNowPlayingInfo(updatedMedia, currentMs)
            lastNowPlayingUpdateMs = currentMs
        }

        // Handle crossfade logic on iOS
        if (settingsRepository.isCrossfadeEnabled && !isCrossfading && currentStatus == PlaybackStatus.PLAYING) {
            if (!durationSec.isNaN() && !currentSec.isNaN() && durationSec > 0.0) {
                val remainingSec = durationSec - currentSec
                val crossfadeSec = settingsRepository.crossfadeDurationSeconds
                val triggerSec = if (settingsRepository.crossfadeStyle == "Radio Segue") {
                    crossfadeSec * 1.5
                } else {
                    crossfadeSec
                }
                if (remainingSec <= triggerSec) {
                    val nextIndex = getNextTrackIndex()
                    if (nextIndex != -1) {
                        startIosCrossfade(crossfadeSec, nextIndex)
                    }
                }
            }
        }
    }

    private fun startIosCrossfade(durationSeconds: Double, nextIndex: Int) {
        val primary = currentPlayer
        val secondary = secondaryPlayer
        isCrossfading = true
 
        if (nextIndex < 0 || nextIndex >= playlistItems.size) {
            isCrossfading = false
            return
        }
        val nextMetadata = playlistItems[nextIndex]

        // Reset biquad delay-line state in eqStorage2 for the new crossfade
        for (i in 0 until 5) {
            val bandOffset = 1 + i * 14
            for (j in 6..13) {
                eqStorage2[bandOffset + j] = 0.0f
            }
        }
        crossfadeJob = scope.launch(Dispatchers.Main) {
            // Always create a fresh item with eqStorage2 for the secondary player.
            // The preloaded item was configured with eqStorage (primary) and cannot
            // be safely reused on the secondary player during crossfade.
            val nextItem = createPlayerItem(nextMetadata.id, forSecondaryPlayer = true)
     
            if (nextItem == null) {
                isCrossfading = false
                return@launch
            }
     
            activePlayerItem = nextItem
            player1.removeItem(nextItem)
            player2.removeItem(nextItem)
            secondary.removeAllItems()
            secondary.insertItem(nextItem, afterItem = null)
            secondary.volume = 0f
     
            // We do NOT swap currentPlayer/secondaryPlayer here.
            // The playerbar continues to show the previous song (primary) and its progress.
     
            val style = settingsRepository.crossfadeStyle
            val intervalMs = 100L
            val durationMs = (durationSeconds * 1000).toLong()
            val totalDurationMs = if (style == "Radio Segue") (durationMs * 1.5).toLong() else durationMs
            val totalSteps = (totalDurationMs / intervalMs).coerceAtLeast(1L)
            val baseSteps = (durationMs / intervalMs).coerceAtLeast(1L)
            val halfSteps = if (style == "Radio Segue") baseSteps / 2 else totalSteps / 2
            var secondaryStarted = false
    
            if (style == "Smooth Blend") {
                secondary.play()
                secondaryStarted = true
            }

            val initialActiveSongId = _playbackState.value.currentMedia?.id
 
            for (step in 1..totalSteps) {
                // Check for manual interventions (e.g. active song id changed)
                if (_playbackState.value.currentMedia?.id != initialActiveSongId) {
                    primary.volume = userVolume
                    secondary.volume = userVolume
                    secondary.pause()
                    isCrossfading = false
                    return@launch
                }
                
                var primaryVol = userVolume
                var secondaryVol = 0f

                when (style) {
                    "Radio Segue" -> {
                        primaryVol = if (step > baseSteps) {
                            val progressPrimary = (step - baseSteps).toFloat() / (totalSteps - baseSteps)
                            val anglePrimary = progressPrimary * (kotlin.math.PI.toFloat() / 2f)
                            userVolume * kotlin.math.cos(anglePrimary)
                        } else {
                            userVolume
                        }

                        secondaryVol = if (step > halfSteps) {
                            if (!secondaryStarted) {
                                secondary.play()
                                secondaryStarted = true
                            }
                            if (step <= baseSteps) {
                                val progressSecondary = (step - halfSteps).toFloat() / (baseSteps - halfSteps)
                                val angleSecondary = progressSecondary * (kotlin.math.PI.toFloat() / 2f)
                                userVolume * kotlin.math.sin(angleSecondary)
                            } else {
                                userVolume
                            }
                        } else {
                            0f
                        }
                    }
                    "Compressed" -> {
                        if (step > halfSteps) {
                            if (!secondaryStarted) {
                                secondary.play()
                                secondaryStarted = true
                            }
                            val progress = (step - halfSteps).toFloat() / (totalSteps - halfSteps)
                            val angle = progress * (kotlin.math.PI.toFloat() / 2f)
                            primaryVol = userVolume * kotlin.math.cos(angle)
                            secondaryVol = userVolume * kotlin.math.sin(angle)
                        } else {
                            primaryVol = userVolume
                            secondaryVol = 0f
                        }
                    }
                    else -> { // "Smooth Blend" (Traditional)
                        val progress = step.toFloat() / totalSteps
                        val angle = progress * (kotlin.math.PI.toFloat() / 2f)
                        primaryVol = userVolume * kotlin.math.cos(angle)
                        secondaryVol = userVolume * kotlin.math.sin(angle)
                    }
                }

                // Normalize volumes to ensure the sum never exceeds userVolume
                val sumVol = primaryVol + secondaryVol
                if (sumVol > userVolume && userVolume > 0f) {
                    primaryVol = (primaryVol * userVolume) / sumVol
                    secondaryVol = (secondaryVol * userVolume) / sumVol
                }

                primary.volume = primaryVol
                secondary.volume = secondaryVol
                
                delay(intervalMs)
            }
 
            // Successful crossfade: pause primary (old player) and restore default volumes
            primary.pause()
            primary.volume = userVolume
            secondary.volume = userVolume
 
            // Swap player roles now that the transition is complete
            currentPlayer = secondary
            secondaryPlayer = primary
 
            // Update state and now playing metadata to show the next song (new player)
            currentIndex = nextIndex
            updateState(PlaybackStatus.PLAYING, nextMetadata)
            lastNowPlayingUpdateMs = 0L
            
            // Get actual elapsed time of the new player to prevent progress jumps
            val currentSec = CMTimeGetSeconds(currentPlayer.currentTime())
            val currentMs = if (!currentSec.isNaN() && currentSec > 0.0) {
                (currentSec * 1000).toLong()
            } else {
                0L
            }
            updateNowPlayingInfo(nextMetadata, currentMs)
 
            isCrossfading = false
            
            preloadedPlayerItem = null
            preloadedSongId = null
            preloadNextTrack()
        }
    }

    // --- System Media Integration (Now Playing & Remote Commands) ---

    private fun setupRemoteCommandCenter() {
        val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()
        
        commandCenter.playCommand.enabled = true
        commandCenter.playCommand.addTargetWithHandler { _ ->
            play()
            MPRemoteCommandHandlerStatusSuccess
        }
        
        commandCenter.pauseCommand.enabled = true
        commandCenter.pauseCommand.addTargetWithHandler { _ ->
            pause()
            MPRemoteCommandHandlerStatusSuccess
        }
        
        commandCenter.togglePlayPauseCommand.enabled = true
        commandCenter.togglePlayPauseCommand.addTargetWithHandler { _ ->
            val state = _playbackState.value.status
            if (state == PlaybackStatus.PLAYING) pause() else play()
            MPRemoteCommandHandlerStatusSuccess
        }
        
        commandCenter.nextTrackCommand.enabled = true
        commandCenter.nextTrackCommand.addTargetWithHandler { _ ->
            skipToNext()
            MPRemoteCommandHandlerStatusSuccess
        }
        
        commandCenter.previousTrackCommand.enabled = true
        commandCenter.previousTrackCommand.addTargetWithHandler { _ ->
            skipToPrevious()
            MPRemoteCommandHandlerStatusSuccess
        }
        
        commandCenter.changePlaybackPositionCommand.enabled = true
        commandCenter.changePlaybackPositionCommand.addTargetWithHandler { event ->
            val posEvent = event as? MPChangePlaybackPositionCommandEvent
            if (posEvent != null) {
                seekTo((posEvent.positionTime * 1000).toLong())
                MPRemoteCommandHandlerStatusSuccess
            } else {
                MPRemoteCommandHandlerStatusCommandFailed
            }
        }
    }

    private fun updateNowPlayingInfo(metadata: MediaMetadata?, positionMs: Long) {
        val nowPlayingInfoCenter = MPNowPlayingInfoCenter.defaultCenter()
        if (metadata == null) {
            nowPlayingInfoCenter.nowPlayingInfo = null
            lastLoadedArtworkUrl = null
            lastLoadedArtwork = null
            return
        }
        
        val title = metadata.title
        val artist = metadata.artist
        val duration = metadata.duration
        val artworkUrl = metadata.artworkUrl
        val isPlaying = _playbackState.value.status == PlaybackStatus.PLAYING
        val rate = if (isPlaying) 1.0 else 0.0

        val cachedImage = if (!artworkUrl.isNullOrEmpty()) artworkCache[artworkUrl] else null
        if (cachedImage != null) {
            lastLoadedArtworkUrl = artworkUrl
            lastLoadedArtwork = cachedImage
        } else if (!artworkUrl.isNullOrEmpty() && artworkUrl != lastLoadedArtworkUrl) {
            lastLoadedArtworkUrl = artworkUrl
            lastLoadedArtwork = null
            
            val nsUrl = NSURL.URLWithString(artworkUrl)
            if (nsUrl != null) {
                val task = NSURLSession.sharedSession.dataTaskWithURL(nsUrl) { data, _, error ->
                    if (error == null && data != null) {
                        val uiImage = UIImage.imageWithData(data)
                        if (uiImage != null) {
                            dispatch_async(dispatch_get_main_queue()) {
                                if (lastLoadedArtworkUrl == artworkUrl) {
                                    if (artworkCache.size >= 15) {
                                        artworkCache.keys.firstOrNull()?.let { artworkCache.remove(it) }
                                    }
                                    artworkCache[artworkUrl] = uiImage
                                    lastLoadedArtwork = uiImage
                                    updateNowPlayingInfo(metadata, positionMs)
                                }
                            }
                        }
                    }
                }
                task.resume()
            }
        }
        
        // Define metadata map to be converted automatically to NSDictionary
        val info = mutableMapOf<Any?, Any?>().apply {
            put(MPMediaItemPropertyTitle, title)
            put(MPMediaItemPropertyArtist, artist)
            put(MPMediaItemPropertyPlaybackDuration, duration / 1000.0)
            put(MPNowPlayingInfoPropertyElapsedPlaybackTime, positionMs / 1000.0)
            put(MPNowPlayingInfoPropertyPlaybackRate, rate)
            
            val loadedArt = lastLoadedArtwork
            if (loadedArt != null) {
                val artwork = MPMediaItemArtwork(loadedArt.size) { _ ->
                    loadedArt
                }
                put(MPMediaItemPropertyArtwork, artwork)
            }
        }
        
        nowPlayingInfoCenter.nowPlayingInfo = info as Map<Any?, *>?
    }

    // --- Audio Interruptions and Route Changes ---

    private fun setupAudioObservers() {
        val notificationCenter = NSNotificationCenter.defaultCenter
        
        // 1. Listen to Audio Interruption Notifications (phone call, alarm)
        interruptionObserver = notificationCenter.addObserverForName(
            name = AVAudioSessionInterruptionNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { notification ->
            val userInfo = notification?.userInfo
            if (userInfo != null) {
                val typeNum = userInfo[AVAudioSessionInterruptionTypeKey] as? NSNumber
                if (typeNum != null) {
                    val type = typeNum.unsignedLongValue
                    if (type == AVAudioSessionInterruptionTypeBegan) {
                        pause()
                    } else if (type == AVAudioSessionInterruptionTypeEnded) {
                        val optionsNum = userInfo[AVAudioSessionInterruptionOptionKey] as? NSNumber
                        val options = optionsNum?.unsignedLongValue ?: 0UL
                        if ((options and AVAudioSessionInterruptionOptionShouldResume) != 0UL) {
                            try {
                                val audioSession = AVAudioSession.sharedInstance()
                                memScoped {
                                    val categoryError = alloc<ObjCObjectVar<NSError?>>()
                                    audioSession.setCategory(AVAudioSessionCategoryPlayback, error = categoryError.ptr)
                                    val activeError = alloc<ObjCObjectVar<NSError?>>()
                                    audioSession.setActive(true, error = activeError.ptr)
                                }
                                play()
                            } catch (e: Exception) {
                                Logger.e(e) { "Audio session setup error" }
                            }
                        }
                    }
                }
            }
        }
        
        // 2. Listen to Audio Route Change Notifications (headphones unplugged)
        routeChangeObserver = notificationCenter.addObserverForName(
            name = AVAudioSessionRouteChangeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { notification ->
            val userInfo = notification?.userInfo
            if (userInfo != null) {
                val reasonNum = userInfo[AVAudioSessionRouteChangeReasonKey] as? NSNumber
                if (reasonNum != null) {
                    val reason = reasonNum.unsignedLongValue
                    if (reason == AVAudioSessionRouteChangeReasonOldDeviceUnavailable) {
                        pause()
                    }
                }
            }
        }
 
        playToEndObserver = notificationCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { notification ->
            if (isCrossfading) return@addObserverForName
            val finishedItem = notification?.`object` as? AVPlayerItem
            if (finishedItem != null && finishedItem == activePlayerItem) {
                if (repeatMode == RepeatMode.ONE) {
                    val metadata = playlistItems.getOrNull(currentIndex)
                    if (metadata != null) {
                        scope.launch(Dispatchers.Main) {
                            val newItem = createPlayerItem(metadata.id)
                            if (newItem != null) {
                                activePlayerItem = newItem
                                currentPlayer.removeAllItems()
                                currentPlayer.insertItem(newItem, afterItem = null)
                                currentPlayer.play()
                                updateNowPlayingInfo(metadata, 0L)
                            }
                        }
                    }
                } else {
                    val nextIndex = getNextTrackIndex()
                    if (nextIndex != -1) {
                        currentIndex = nextIndex
                        val nextMetadata = playlistItems[currentIndex]
                        
                        if (preloadedPlayerItem != null && preloadedSongId == nextMetadata.id) {
                            // The player has automatically transitioned to the next item
                            activePlayerItem = preloadedPlayerItem
                            updateState(PlaybackStatus.PLAYING, nextMetadata)
                            updateNowPlayingInfo(nextMetadata, 0L)
                            
                            preloadedPlayerItem = null
                            preloadedSongId = null
                            preloadNextTrack()
                        } else {
                            // Fallback if preloading is not ready yet
                            playTrack(nextMetadata)
                        }
                    } else {
                        stop()
                    }
                }
            }
        }

        // 4. Listen for playback stalls and attempt recovery
        notificationCenter.addObserverForName(
            name = AVPlayerItemPlaybackStalledNotification,
            `object` = null,
            queue = NSOperationQueue.mainQueue
        ) { _ ->
            Logger.w { "Playback stalled, attempting recovery..." }
            scope.launch(Dispatchers.Main) {
                delay(1000)
                if (_playbackState.value.status == PlaybackStatus.PLAYING) {
                    currentPlayer.play()
                }
            }
        }
    }

    // --- Network Monitor using SCNetworkReachability ---

    private fun monitorNetwork() {
        val reachability = memScoped {
            val zeroAddress = alloc<sockaddr_in>()
            zeroAddress.sin_len = sizeOf<sockaddr_in>().toUByte()
            zeroAddress.sin_family = AF_INET.toUByte()
            SCNetworkReachabilityCreateWithAddress(null, zeroAddress.ptr.reinterpret())
        } ?: return
        
        val flags = memScoped {
            val flagsVar = alloc<kotlinx.cinterop.UIntVar>()
            if (SCNetworkReachabilityGetFlags(reachability, flagsVar.ptr)) {
                flagsVar.value
            } else {
                0u
            }
        }
        
        val isReachable = (flags and kSCNetworkReachabilityFlagsReachable) != 0u
        val needsConnection = (flags and kSCNetworkReachabilityFlagsConnectionRequired) != 0u
        val active = isReachable && !needsConnection
        
        if (_isConnected.value != active) {
            _isConnected.value = active
            _networkError.value = !active
        }
        CFRelease(reachability)
    }

    override fun release() {
        cancelCrossfade()
        scope.cancel()
        preloadJob?.cancel()
        
        activeDownloads.values.forEach { it.cancel() }
        activeDownloads.clear()
        
        timeObserverToken?.let {
            player1.removeTimeObserver(it)
        }
        timeObserverToken = null
        timeObserverToken2?.let {
            player2.removeTimeObserver(it)
        }
        timeObserverToken2 = null
        
        val notificationCenter = NSNotificationCenter.defaultCenter
        interruptionObserver?.let { notificationCenter.removeObserver(it) }
        routeChangeObserver?.let { notificationCenter.removeObserver(it) }
        playToEndObserver?.let { notificationCenter.removeObserver(it) }
        
        interruptionObserver = null
        routeChangeObserver = null
        playToEndObserver = null
        
        val commandCenter = MPRemoteCommandCenter.sharedCommandCenter()
        commandCenter.playCommand.enabled = false
        commandCenter.playCommand.removeTarget(null)
        commandCenter.pauseCommand.enabled = false
        commandCenter.pauseCommand.removeTarget(null)
        commandCenter.togglePlayPauseCommand.enabled = false
        commandCenter.togglePlayPauseCommand.removeTarget(null)
        commandCenter.nextTrackCommand.enabled = false
        commandCenter.nextTrackCommand.removeTarget(null)
        commandCenter.previousTrackCommand.enabled = false
        commandCenter.previousTrackCommand.removeTarget(null)
        commandCenter.changePlaybackPositionCommand.enabled = false
        commandCenter.changePlaybackPositionCommand.removeTarget(null)

        player1.pause()
        player1.removeAllItems()
        player2.pause()
        player2.removeAllItems()
        activePlayerItem = null
        
        artworkCache.clear()
        lastLoadedArtwork = null

        nativeHeap.free(eqStorage)
        nativeHeap.free(eqStorage2)
    }
}
