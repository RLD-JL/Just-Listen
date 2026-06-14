@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
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
    private var crossfadeTargetVolume = 1f
    private var crossfadeJob: kotlinx.coroutines.Job? = null
    private var timeObserverToken2: Any? = null

    private fun cancelCrossfade(pauseActivePlayer: Boolean = false) {
        crossfadeJob?.cancel()
        crossfadeJob = null
        
        if (isCrossfading) {
            // Restore default volumes
            player1.volume = crossfadeTargetVolume
            player2.volume = crossfadeTargetVolume
            
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
    
    private var playlistItems = mutableListOf<MediaMetadata>()
    private var currentIndex = -1
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private var shuffledIndices = listOf<Int>()
    private var isShuffleEnabled = false
    private var repeatMode = RepeatMode.NONE
    private var favoriteIdsSet = emptySet<String>()

    private val eqStorage = nativeHeap.allocArray<FloatVar>(73).apply {
        this[0] = 0.0f
        this[71] = 0.0f
        this[72] = 1.0f
    }
    
    private var lastLoadedArtworkUrl: String? = null
    private var lastLoadedArtwork: UIImage? = null
    private var timeObserverToken: Any? = null
    
    private val activeDownloads = mutableMapOf<String, NSURLSessionDownloadTask>()
    
    private var interruptionObserver: Any? = null
    private var routeChangeObserver: Any? = null
    private var playToEndObserver: Any? = null
    private val artworkCache = mutableMapOf<String, UIImage>()


    // Public property to allow volume fading from Sleep Timer
    var volume: Float
        get() = currentPlayer.volume
        set(value) {
            currentPlayer.volume = value
        }

    init {
        scope.launch {
            settingsViewModel.settingsState.collect { state ->
                eqStorage[0] = if (state.isEqEnabled) 1.0f else 0.0f
                for (i in 0 until 5) {
                    val bandOffset = 1 + i * 14
                    if (i < state.eqBands.size) {
                        eqStorage[bandOffset + 0] = state.eqBands[i]
                    }
                }
                eqStorage[72] = 1.0f
            }
        }

        try {
            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayback, error = null)
            audioSession.setActive(true, error = null)
        } catch (e: Exception) {
            Logger.e(e) { "Audio session category set error" }
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
                            cleanCache()
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

    private fun createPlayerItem(songId: String): AVPlayerItem? {
        val cachedUrl = getCacheFileUrl(songId)
        val playerItem = if (cachedUrl != null && cachedUrl.path != null && NSFileManager.defaultManager.fileExistsAtPath(cachedUrl.path!!)) {
            AVPlayerItem.playerItemWithURL(cachedUrl)
        } else {
            val baseUrl = com.rld.justlisten.datalayer.utils.Constants.BASEURL
            val appName = com.rld.justlisten.datalayer.utils.Constants.appName.replace(" ", "%20")
            val streamUrl = "$baseUrl/v1/tracks/$songId/stream?app_name=$appName"
            val nsUrl = NSURL.URLWithString(streamUrl)
            if (nsUrl != null) {
                triggerBackgroundDownload(songId, nsUrl)
                AVPlayerItem.playerItemWithURL(nsUrl)
            } else {
                null
            }
        }
        if (playerItem != null) {
            configureAudioProcessingTap(playerItem)
        }
        return playerItem
    }

    private fun configureAudioProcessingTap(playerItem: AVPlayerItem) {
        memScoped {
            val callbacks = alloc<MTAudioProcessingTapCallbacks>()
            callbacks.version = kMTAudioProcessingTapCallbacksVersion_0
            callbacks.clientInfo = eqStorage
            
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
                if (status.toInt() != 0 || bufferListInOut == null) {
                    return@staticCFunction
                }

                val statePtr = MTAudioProcessingTapGetStorage(tap)?.reinterpret<FloatVar>() ?: return@staticCFunction
                val isEnabled = statePtr[0] > 0.5f
                val framesToProcess = numberFramesOut?.pointed?.value ?: numberFrames

                if (statePtr[72] > 0.5f) {
                    val sampleRate = statePtr[71]
                    if (sampleRate > 0f) {
                        val centerFreqs = floatArrayOf(60f, 230f, 910f, 4000f, 14000f)
                        val Qs = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f, 1.0f)
                        for (i in 0 until 5) {
                            val bandOffset = 1 + i * 14
                            val gainDb = statePtr[bandOffset + 0]
                            val A = 10.0.pow(gainDb.toDouble() / 40.0).toFloat()
                            val w0 = (2.0f * kotlin.math.PI.toFloat() * centerFreqs[i] / sampleRate)
                            val cosW0 = kotlin.math.cos(w0)
                            val sinW0 = kotlin.math.sin(w0)
                            val alpha = sinW0 / (2.0f * Qs[i])

                            val b0 = 1.0f + alpha * A
                            val b1 = -2.0f * cosW0
                            val b2 = 1.0f - alpha * A
                            val a0 = 1.0f + alpha / A
                            val a1 = -2.0f * cosW0
                            val a2 = 1.0f - alpha / A

                            statePtr[bandOffset + 1] = b0 / a0
                            statePtr[bandOffset + 2] = b1 / a0
                            statePtr[bandOffset + 3] = b2 / a0
                            statePtr[bandOffset + 4] = a1 / a0
                            statePtr[bandOffset + 5] = a2 / a0
                        }
                        statePtr[72] = 0.0f
                    }
                }

                if (!isEnabled) {
                    return@staticCFunction
                }

                val bufferList = bufferListInOut.pointed
                val numBuffers = bufferList.mNumberBuffers.toInt()
                
                for (b in 0 until numBuffers) {
                    val audioBuffer = bufferList.mBuffers[b]
                    val mData = audioBuffer.mData?.reinterpret<FloatVar>() ?: continue
                    val channels = audioBuffer.mNumberChannels.toInt()
                    
                    if (channels == 2) {
                        for (f in 0 until framesToProcess.toInt()) {
                            var sampleL = mData[f * 2]
                            var sampleR = mData[f * 2 + 1]
                            
                            for (i in 0 until 5) {
                                val bandOffset = 1 + i * 14
                                val b0 = statePtr[bandOffset + 1]
                                val b1 = statePtr[bandOffset + 2]
                                val b2 = statePtr[bandOffset + 3]
                                val a1 = statePtr[bandOffset + 4]
                                val a2 = statePtr[bandOffset + 5]
                                
                                val x1_l = statePtr[bandOffset + 6]
                                val x2_l = statePtr[bandOffset + 7]
                                val y1_l = statePtr[bandOffset + 8]
                                val y2_l = statePtr[bandOffset + 9]
                                
                                val outL = b0 * sampleL + b1 * x1_l + b2 * x2_l - a1 * y1_l - a2 * y2_l
                                statePtr[bandOffset + 7] = x1_l
                                statePtr[bandOffset + 6] = sampleL
                                statePtr[bandOffset + 9] = y1_l
                                statePtr[bandOffset + 8] = outL
                                sampleL = outL
                                
                                val x1_r = statePtr[bandOffset + 10]
                                val x2_r = statePtr[bandOffset + 11]
                                val y1_r = statePtr[bandOffset + 12]
                                val y2_r = statePtr[bandOffset + 13]
                                
                                val outR = b0 * sampleR + b1 * x1_r + b2 * x2_r - a1 * y1_r - a2 * y2_r
                                statePtr[bandOffset + 11] = x1_r
                                statePtr[bandOffset + 10] = sampleR
                                statePtr[bandOffset + 13] = y1_r
                                statePtr[bandOffset + 12] = outR
                                sampleR = outR
                            }
                            
                            mData[f * 2] = sampleL
                            mData[f * 2 + 1] = sampleR
                        }
                    } else {
                        val chIndex = if (b == 0) 0 else 1
                        for (f in 0 until framesToProcess.toInt()) {
                            var sample = mData[f]
                            
                            for (i in 0 until 5) {
                                val bandOffset = 1 + i * 14
                                val b0 = statePtr[bandOffset + 1]
                                val b1 = statePtr[bandOffset + 2]
                                val b2 = statePtr[bandOffset + 3]
                                val a1 = statePtr[bandOffset + 4]
                                val a2 = statePtr[bandOffset + 5]
                                
                                val x1Idx = if (chIndex == 0) 6 else 10
                                val x2Idx = if (chIndex == 0) 7 else 11
                                val y1Idx = if (chIndex == 0) 8 else 12
                                val y2Idx = if (chIndex == 0) 9 else 13
                                
                                val x1 = statePtr[bandOffset + x1Idx]
                                val x2 = statePtr[bandOffset + x2Idx]
                                val y1 = statePtr[bandOffset + y1Idx]
                                val y2 = statePtr[bandOffset + y2Idx]
                                
                                val out = b0 * sample + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
                                statePtr[bandOffset + x2Idx] = x1
                                statePtr[bandOffset + x1Idx] = sample
                                statePtr[bandOffset + y2Idx] = y1
                                statePtr[bandOffset + y1Idx] = out
                                sample = out
                            }
                            
                            mData[f] = sample
                        }
                    }
                }
            }

            val tapVar = alloc<MTAudioProcessingTapRefVar>()
            val status = MTAudioProcessingTapCreate(null, callbacks.ptr, kMTAudioProcessingTapCreationFlag_PreEffects, tapVar.ptr)
            if (status.toInt() == 0) {
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
        val playerItem = if (preloadedSongId == metadata.id && preloadedPlayerItem != null) {
            preloadedPlayerItem!!
        } else {
            createPlayerItem(metadata.id)
        }

        if (playerItem != null) {
            activePlayerItem = playerItem
            currentPlayer.removeAllItems()
            currentPlayer.insertItem(playerItem, afterItem = null)
            currentPlayer.play()
            updateState(PlaybackStatus.BUFFERING, metadata)
            updateNowPlayingInfo(metadata, 0L)
            
            preloadedPlayerItem = null
            preloadedSongId = null
            preloadNextTrack()
        } else {
            updateState(PlaybackStatus.ERROR)
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
                duration = 0L,
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
                duration = 0L,
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

        val updatedMedia = currentMedia?.copy(duration = durationMs)

        _playbackState.update { state ->
            state.copy(
                status = currentStatus,
                currentPosition = currentMs,
                currentMedia = updatedMedia
            )
        }

        // Update system now playing info center when duration is resolved or playback status changes
        if (updatedMedia != null && (durationMs != oldDuration || currentStatus != oldStatus)) {
            updateNowPlayingInfo(updatedMedia, currentMs)
        }

        // Handle crossfade logic on iOS
        if (settingsRepository.isCrossfadeEnabled && !isCrossfading && currentStatus == PlaybackStatus.PLAYING) {
            if (!durationSec.isNaN() && !currentSec.isNaN() && durationSec > 0.0) {
                val remainingSec = durationSec - currentSec
                val crossfadeSec = settingsRepository.crossfadeDurationSeconds
                if (remainingSec <= crossfadeSec) {
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
        crossfadeTargetVolume = primary.volume
 
        val nextMetadata = playlistItems[nextIndex]
        val nextItem = if (preloadedSongId == nextMetadata.id && preloadedPlayerItem != null) {
            preloadedPlayerItem!!
        } else {
            createPlayerItem(nextMetadata.id)
        }
 
        if (nextItem == null) {
            isCrossfading = false
            return
        }
 
        activePlayerItem = nextItem
        secondary.removeAllItems()
        secondary.insertItem(nextItem, afterItem = null)
        secondary.volume = 0f
        secondary.play()
 
        // We do NOT swap currentPlayer/secondaryPlayer here.
        // The playerbar continues to show the previous song (primary) and its progress.
 
        crossfadeJob = scope.launch(Dispatchers.Main) {
            val durationMs = (durationSeconds * 1000).toLong()
            val intervalMs = 100L
            val totalSteps = (durationMs / intervalMs).coerceAtLeast(1L)
            val initialActiveSongId = _playbackState.value.currentMedia?.id
 
            for (step in 1..totalSteps) {
                // Check for manual interventions on the active (old) player, new player, or active song id change
                if (primary.rate == 0.0f || secondary.rate == 0.0f || _playbackState.value.currentMedia?.id != initialActiveSongId) {
                    primary.volume = crossfadeTargetVolume
                    secondary.volume = crossfadeTargetVolume
                    secondary.pause()
                    isCrossfading = false
                    return@launch
                }
                
                // Equal-power crossfade curve scaled by target volume
                val progress = step.toFloat() / totalSteps
                val angle = progress * (kotlin.math.PI.toFloat() / 2f)
                primary.volume = crossfadeTargetVolume * kotlin.math.cos(angle)
                secondary.volume = crossfadeTargetVolume * kotlin.math.sin(angle)
                
                delay(intervalMs)
            }
 
            // Successful crossfade: pause primary (old player) and restore default volumes
            primary.pause()
            primary.volume = crossfadeTargetVolume
            secondary.volume = crossfadeTargetVolume
 
            // Swap player roles now that the transition is complete
            currentPlayer = secondary
            secondaryPlayer = primary
 
            // Update state and now playing metadata to show the next song (new player)
            currentIndex = nextIndex
            updateState(PlaybackStatus.PLAYING, nextMetadata)
            updateNowPlayingInfo(nextMetadata, 0L)
 
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
                                        artworkCache.clear()
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
            queue = null
        ) { notification ->
            val userInfo = notification?.userInfo
            if (userInfo != null) {
                val typeNum = userInfo[AVAudioSessionInterruptionTypeKey] as? NSNumber
                if (typeNum != null) {
                    val type = typeNum.unsignedLongValue
                    dispatch_async(dispatch_get_main_queue()) {
                        if (type == AVAudioSessionInterruptionTypeBegan) {
                            pause()
                        } else if (type == AVAudioSessionInterruptionTypeEnded) {
                            val optionsNum = userInfo[AVAudioSessionInterruptionOptionKey] as? NSNumber
                            val options = optionsNum?.unsignedLongValue ?: 0UL
                            if ((options and AVAudioSessionInterruptionOptionShouldResume) != 0UL) {
                                play()
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
            queue = null
        ) { notification ->
            val userInfo = notification?.userInfo
            if (userInfo != null) {
                val reasonNum = userInfo[AVAudioSessionRouteChangeReasonKey] as? NSNumber
                if (reasonNum != null) {
                    val reason = reasonNum.unsignedLongValue
                    if (reason == AVAudioSessionRouteChangeReasonOldDeviceUnavailable) {
                        dispatch_async(dispatch_get_main_queue()) {
                            pause()
                        }
                    }
                }
            }
        }
 
        playToEndObserver = notificationCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = null,
            queue = null
        ) { notification ->
            dispatch_async(dispatch_get_main_queue()) {
                if (isCrossfading) return@dispatch_async
                val finishedItem = notification?.`object` as? AVPlayerItem
                if (finishedItem != null && finishedItem == activePlayerItem) {
                    if (repeatMode == RepeatMode.ONE) {
                        activePlayerItem?.let { item ->
                            currentPlayer.removeAllItems()
                            currentPlayer.insertItem(item, afterItem = null)
                            seekTo(0L)
                            currentPlayer.play()
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
    }
}
