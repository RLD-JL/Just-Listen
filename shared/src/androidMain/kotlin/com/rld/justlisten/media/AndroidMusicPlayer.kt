package com.rld.justlisten.media

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.rld.justlisten.media.exoplayer.MusicServiceConnection
import com.rld.justlisten.media.exoplayer.currentPlaybackPosition
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AndroidMusicPlayer(
    val musicServiceConnection: MusicServiceConnection
) : MusicPlayer {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _playbackState = MutableStateFlow(PlaybackState(PlaybackStatus.IDLE, 0L))
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

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
                musicServiceConnection.currentPlayingSong
            ) { state, metadata ->
                updateState(state, metadata)
            }.collect()
        }

        scope.launch {
            while (isActive) {
                val state = _playbackState.value
                if (state.status == PlaybackStatus.PLAYING && !musicServiceConnection.sliderClicked.value) {
                    val currentPos = musicServiceConnection.playbackState.value?.currentPlaybackPosition ?: 0L
                    _playbackState.value = state.copy(currentPosition = currentPos)
                }
                delay(250L)
            }
        }
    }

    override fun play() {
        musicServiceConnection.transportControls?.play()
    }

    override fun pause() {
        musicServiceConnection.transportControls?.pause()
    }

    override fun stop() {
        musicServiceConnection.transportControls?.stop()
    }

    override fun skipToNext() {
        musicServiceConnection.transportControls?.skipToNext()
    }

    override fun skipToPrevious() {
        musicServiceConnection.transportControls?.skipToPrevious()
    }

    override fun seekTo(position: Long) {
        musicServiceConnection.transportControls?.seekTo(position)
    }

    override fun setShuffleModeEnabled(enabled: Boolean) {
        musicServiceConnection.transportControls?.setShuffleMode(
            if (enabled) PlaybackStateCompat.SHUFFLE_MODE_ALL else PlaybackStateCompat.SHUFFLE_MODE_NONE
        )
    }

    override fun setRepeatMode(repeatMode: RepeatMode) {
        val mode = when (repeatMode) {
            RepeatMode.NONE -> PlaybackStateCompat.REPEAT_MODE_NONE
            RepeatMode.ONE -> PlaybackStateCompat.REPEAT_MODE_ONE
            RepeatMode.ALL -> PlaybackStateCompat.REPEAT_MODE_ALL
        }
        musicServiceConnection.transportControls?.setRepeatMode(mode)
    }

    override fun playMedia(mediaId: String) {
        scope.launch {
            var attempts = 0
            while (musicServiceConnection.transportControls == null && attempts < 30) {
                delay(100)
                attempts++
            }
            musicServiceConnection.transportControls?.playFromMediaId(mediaId, null)
        }
    }

    override fun updatePlaylist(list: List<com.rld.justlisten.viewmodel.interfaces.Item>) {
        musicServiceConnection.updatePlaylist(list)
    }
    
    // Helper to update internal state from MusicServiceConnection
    private fun updateState(compatState: PlaybackStateCompat?, metadata: MediaMetadataCompat?) {
        if (!musicServiceConnection.isConnected.value) return
        
        _playbackState.value = PlaybackState(
            status = mapStatus(compatState?.state),
            currentPosition = compatState?.currentPlaybackPosition ?: 0L,
            currentMedia = mapMetadata(metadata),
            isShuffleModeEnabled = musicServiceConnection.shuffleMode != PlaybackStateCompat.SHUFFLE_MODE_NONE,
            repeatMode = mapRepeatMode(musicServiceConnection.repeatMode)
        )
    }
    
    private fun mapStatus(state: Int?): PlaybackStatus = when (state) {
        PlaybackStateCompat.STATE_PLAYING -> PlaybackStatus.PLAYING
        PlaybackStateCompat.STATE_PAUSED -> PlaybackStatus.PAUSED
        PlaybackStateCompat.STATE_BUFFERING -> PlaybackStatus.BUFFERING
        PlaybackStateCompat.STATE_STOPPED -> PlaybackStatus.STOPPED
        PlaybackStateCompat.STATE_ERROR -> PlaybackStatus.ERROR
        else -> PlaybackStatus.IDLE
    }
    
    private fun mapMetadata(compat: MediaMetadataCompat?): MediaMetadata? {
        if (compat == null) return null
        return MediaMetadata(
            id = compat.description.mediaId ?: "",
            title = compat.description.title?.toString() ?: "",
            artist = compat.description.subtitle?.toString() ?: "",
            duration = compat.getLong(MediaMetadataCompat.METADATA_KEY_DURATION),
            artworkUrl = compat.getString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI) ?: compat.description.iconUri?.toString(),
            lowResArtworkUrl = compat.getString(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON_URI) ?: compat.description.iconUri?.toString()
        )
    }
    
    private fun mapRepeatMode(mode: Int): RepeatMode = when (mode) {
        PlaybackStateCompat.REPEAT_MODE_ONE -> RepeatMode.ONE
        PlaybackStateCompat.REPEAT_MODE_ALL -> RepeatMode.ALL
        else -> RepeatMode.NONE
    }
}
