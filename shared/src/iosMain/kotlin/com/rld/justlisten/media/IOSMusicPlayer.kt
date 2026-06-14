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


class IOSMusicPlayer(
    private val favoritesRepository: FavoritesRepository
) : MusicPlayer {
    private val avPlayer = AVQueuePlayer()
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
    
    private var lastLoadedArtworkUrl: String? = null
    private var lastLoadedArtwork: UIImage? = null
    
    private var interruptionObserver: Any? = null
    private var routeChangeObserver: Any? = null
    private var playToEndObserver: Any? = null
    private val artworkCache = mutableMapOf<String, UIImage>()


    // Public property to allow volume fading from Sleep Timer
    var volume: Float
        get() = avPlayer.volume
        set(value) {
            avPlayer.volume = value
        }

    init {
        try {
            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryPlayback, error = null)
            audioSession.setActive(true, error = null)
        } catch (e: Exception) {
            Logger.e(e) { "Audio session category set error" }
        }

        // Periodically update progress and monitor network
        scope.launch {
            while (isActive) {
                updateProgress()
                monitorNetwork()
                delay(250L)
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
        avPlayer.play()
        updateState(PlaybackStatus.PLAYING)
        updateNowPlayingInfo(_playbackState.value.currentMedia, _playbackState.value.currentPosition)
    }

    override fun pause() {
        avPlayer.pause()
        updateState(PlaybackStatus.PAUSED)
        updateNowPlayingInfo(_playbackState.value.currentMedia, _playbackState.value.currentPosition)
    }

    override fun stop() {
        avPlayer.pause()
        avPlayer.removeAllItems()
        activePlayerItem = null
        preloadedPlayerItem = null
        preloadedSongId = null
        preloadJob?.cancel()
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

    private fun createPlayerItem(songId: String): AVPlayerItem? {
        val baseUrl = com.rld.justlisten.datalayer.utils.Constants.BASEURL
        val appName = com.rld.justlisten.datalayer.utils.Constants.appName.replace(" ", "%20")
        val streamUrl = "$baseUrl/v1/tracks/$songId/stream?app_name=$appName"
        val nsUrl = NSURL.URLWithString(streamUrl)
        return if (nsUrl != null) AVPlayerItem.playerItemWithURL(nsUrl) else null
    }

    private fun playTrack(metadata: MediaMetadata) {
        val playerItem = if (preloadedSongId == metadata.id && preloadedPlayerItem != null) {
            preloadedPlayerItem!!
        } else {
            createPlayerItem(metadata.id)
        }

        if (playerItem != null) {
            activePlayerItem = playerItem
            avPlayer.removeAllItems()
            avPlayer.insertItem(playerItem, afterItem = null)
            avPlayer.play()
            updateState(PlaybackStatus.BUFFERING, metadata)
            updateNowPlayingInfo(metadata, 0L)
            
            preloadedPlayerItem = null
            preloadedSongId = null
            preloadNextTrack()
        } else {
            updateState(PlaybackStatus.ERROR)
        }
    }

    private fun preloadNextTrack() {
        preloadJob?.cancel()
        
        val nextIndex = getNextTrackIndex()
        if (nextIndex == -1 || nextIndex >= playlistItems.size) {
            preloadedPlayerItem = null
            preloadedSongId = null
            return
        }
        
        val nextMetadata = playlistItems[nextIndex]
        
        preloadJob = scope.launch(Dispatchers.Main) {
            delay(2000L)
            
            if (repeatMode == RepeatMode.ONE) {
                preloadedPlayerItem = null
                preloadedSongId = null
                return@launch
            }
            
            val nextItem = createPlayerItem(nextMetadata.id)
            if (nextItem != null) {
                preloadedPlayerItem = nextItem
                preloadedSongId = nextMetadata.id
                
                val currentActiveItem = avPlayer.currentItem
                if (currentActiveItem != null && avPlayer.items().size == 1) {
                    avPlayer.insertItem(nextItem, afterItem = currentActiveItem)
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
        val nextIndex = getNextTrackIndex()
        if (nextIndex != -1) {
            val nextItem = playlistItems[nextIndex]
            if (avPlayer.items().size > 1 && preloadedSongId == nextItem.id && preloadedPlayerItem != null) {
                avPlayer.advanceToNextItem()
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
        val time = CMTimeMake(position, 1000)
        avPlayer.seekToTime(time)
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
            val items = avPlayer.items()
            if (items.size > 1) {
                for (i in 1 until items.size) {
                    val item = items[i] as? AVPlayerItem
                    if (item != null) avPlayer.removeItem(item)
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
        val player = avPlayer
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
            
            scope.launch(Dispatchers.Default) {
                try {
                    val nsUrl = NSURL.URLWithString(artworkUrl)
                    if (nsUrl != null) {
                        val data = NSData.dataWithContentsOfURL(nsUrl)
                        if (data != null) {
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
                } catch (e: Exception) {
                    Logger.e(e) { "Error loading lockscreen artwork" }
                }
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
 
        // 3. Listen to Track Finished Notification
        playToEndObserver = notificationCenter.addObserverForName(
            name = AVPlayerItemDidPlayToEndTimeNotification,
            `object` = null,
            queue = null
        ) { notification ->
            dispatch_async(dispatch_get_main_queue()) {
                val finishedItem = notification?.`object` as? AVPlayerItem
                if (finishedItem != null && finishedItem == activePlayerItem) {
                    if (repeatMode == RepeatMode.ONE) {
                        seekTo(0L)
                        avPlayer.play()
                    } else {
                        val nextIndex = getNextTrackIndex()
                        if (nextIndex != -1) {
                            currentIndex = nextIndex
                            val nextMetadata = playlistItems[currentIndex]
                            
                            // The player has automatically transitioned to the next item
                            activePlayerItem = preloadedPlayerItem
                            updateState(PlaybackStatus.PLAYING, nextMetadata)
                            updateNowPlayingInfo(nextMetadata, 0L)
                            
                            preloadedPlayerItem = null
                            preloadedSongId = null
                            preloadNextTrack()
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
        scope.cancel()
        preloadJob?.cancel()
        
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

        avPlayer.pause()
        avPlayer.removeAllItems()
        activePlayerItem = null
        
        artworkCache.clear()
        lastLoadedArtwork = null
    }
}
