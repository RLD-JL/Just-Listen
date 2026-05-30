package com.rld.justlisten.media

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.rld.justlisten.media.exoplayer.MusicServiceConnection
import com.rld.justlisten.datalayer.datacalls.library.getFavoritePlaylistWithId
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AndroidMusicPlayer(
    val musicServiceConnection: MusicServiceConnection,
    private val repository: com.rld.justlisten.datalayer.Repository
) : MusicPlayer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _playbackState = MutableStateFlow(PlaybackState(PlaybackStatus.IDLE, 0L))
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<MediaMetadata>>(emptyList())
    override val currentPlaylist: StateFlow<List<MediaMetadata>> = _currentPlaylist.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _networkError = MutableStateFlow(false)
    override val networkError: StateFlow<Boolean> = _networkError.asStateFlow()

    init {
        scope.launch {
            musicServiceConnection.isConnected.collect { _isConnected.value = it }
        }
        scope.launch {
            musicServiceConnection.networkError.collect { _networkError.value = it }
        }
        scope.launch {
            combine(
                musicServiceConnection.playbackState,
                musicServiceConnection.currentPlayingSong,
                musicServiceConnection.playWhenReady,
                musicServiceConnection.shuffleModeEnabled,
                musicServiceConnection.repeatMode,
                musicServiceConnection.currentPlaylist
            ) { args ->
                val state = args[0] as Int
                val mediaItem = args[1] as? MediaItem
                val playlist = args[5] as List<MediaItem>
                updateState(state, mediaItem)
                _currentPlaylist.value = playlist.mapNotNull { mapMetadata(it) }
            }.collect()
        }

        scope.launch {
            repository.getFavoritePlaylistFlow().collect { favoriteList ->
                val favoriteIds = favoriteList.map { it.id }.toSet()
                if (favoriteIds.contains(_playbackState.value.currentMedia?.id)) {
                    refreshMetadata()
                }
            }
        }

        scope.launch {
            while (isActive) {
                val state = _playbackState.value
                if (state.status == PlaybackStatus.PLAYING && !musicServiceConnection.sliderClicked.value) {
                    val currentPos = musicServiceConnection.mediaController?.currentPosition ?: 0L
                    _playbackState.value = state.copy(currentPosition = currentPos)
                }
                delay(250L)
            }
        }
    }

    override fun play() {
        musicServiceConnection.mediaController?.play()
    }

    override fun pause() {
        musicServiceConnection.mediaController?.pause()
    }

    override fun stop() {
        musicServiceConnection.mediaController?.stop()
    }

    override fun skipToNext() {
        musicServiceConnection.mediaController?.seekToNext()
    }

    override fun skipToPrevious() {
        musicServiceConnection.mediaController?.seekToPrevious()
    }

    override fun seekTo(position: Long) {
        musicServiceConnection.mediaController?.seekTo(position)
    }

    override fun setShuffleModeEnabled(enabled: Boolean) {
        musicServiceConnection.mediaController?.shuffleModeEnabled = enabled
    }

    override fun setRepeatMode(repeatMode: RepeatMode) {
        val mode = when (repeatMode) {
            RepeatMode.NONE -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
        musicServiceConnection.mediaController?.repeatMode = mode
    }

    override fun playMedia(mediaId: String) {
        scope.launch {
            var attempts = 0
            while (musicServiceConnection.mediaController == null && attempts < 30) {
                delay(100)
                attempts++
            }
            musicServiceConnection.mediaController?.let { controller ->
                for (i in 0 until controller.mediaItemCount) {
                    if (controller.getMediaItemAt(i).mediaId == mediaId) {
                        controller.seekTo(i, 0)
                        controller.play()
                        break
                    }
                }
            }
        }
    }

    override fun updatePlaylist(list: List<com.rld.justlisten.viewmodel.interfaces.Item>) {
        musicServiceConnection.updatePlaylist(list)
    }

    override fun refreshMetadata() {
        updateState(musicServiceConnection.playbackState.value, musicServiceConnection.currentPlayingSong.value)
    }

    override fun removeTrack(index: Int) {
        musicServiceConnection.mediaController?.let { controller ->
            if (index in 0 until controller.mediaItemCount) {
                controller.removeMediaItem(index)
            }
        }
    }

    override fun moveTrack(fromIndex: Int, toIndex: Int) {
        musicServiceConnection.mediaController?.let { controller ->
            if (fromIndex in 0 until controller.mediaItemCount && toIndex in 0 until controller.mediaItemCount) {
                controller.moveMediaItem(fromIndex, toIndex)
            }
        }
    }
    
    // Helper to update internal state from MusicServiceConnection
    private fun updateState(state: Int, mediaItem: MediaItem?) {
        if (!musicServiceConnection.isConnected.value) return
        
        _playbackState.value = PlaybackState(
            status = mapStatus(state),
            currentPosition = musicServiceConnection.mediaController?.currentPosition ?: 0L,
            currentMedia = mapMetadata(mediaItem),
            isShuffleModeEnabled = musicServiceConnection.mediaController?.shuffleModeEnabled ?: false,
            repeatMode = mapRepeatMode(musicServiceConnection.mediaController?.repeatMode ?: Player.REPEAT_MODE_OFF)
        )
    }
    
    private fun mapStatus(state: Int): PlaybackStatus = when (state) {
        Player.STATE_READY -> if (musicServiceConnection.mediaController?.playWhenReady == true) PlaybackStatus.PLAYING else PlaybackStatus.PAUSED
        Player.STATE_BUFFERING -> PlaybackStatus.BUFFERING
        Player.STATE_IDLE -> PlaybackStatus.IDLE
        Player.STATE_ENDED -> PlaybackStatus.STOPPED
        else -> PlaybackStatus.IDLE
    }
    
    private fun mapMetadata(mediaItem: MediaItem?): MediaMetadata? {
        if (mediaItem == null) return null
        val id = mediaItem.mediaId
        val metadata = mediaItem.mediaMetadata
        return MediaMetadata(
            id = id,
            title = metadata.title?.toString() ?: "",
            artist = metadata.artist?.toString() ?: "",
            duration = musicServiceConnection.mediaController?.duration ?: 0L,
            artworkUrl = metadata.artworkUri?.toString(),
            lowResArtworkUrl = metadata.artworkUri?.toString(),
            isFavorite = repository.getFavoritePlaylistWithId(id) != null
        )
    }
    
    private fun mapRepeatMode(mode: Int): RepeatMode = when (mode) {
        Player.REPEAT_MODE_ONE -> RepeatMode.ONE
        Player.REPEAT_MODE_ALL -> RepeatMode.ALL
        else -> RepeatMode.NONE
    }
}
