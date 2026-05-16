package com.rld.justlisten.media

import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import com.rld.justlisten.media.exoplayer.MusicServiceConnection
import com.rld.justlisten.media.exoplayer.currentPlaybackPosition
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
            // In a real app, we would use a more robust way to sync states
            // For this migration, we'll poll or hook into the existing MutableState
            while(true) {
                updateState()
                _isConnected.value = musicServiceConnection.isConnected.value
                _networkError.value = musicServiceConnection.networkError.value
                kotlinx.coroutines.delay(500)
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
                kotlinx.coroutines.delay(100)
                attempts++
            }
            musicServiceConnection.transportControls?.playFromMediaId(mediaId, null)
        }
    }

    override fun updatePlaylist(list: List<com.rld.justlisten.viewmodel.interfaces.Item>) {
        musicServiceConnection.updatePlaylist(list)
    }
    
    // Helper to update internal state from MusicServiceConnection
    fun updateState() {
        if (!musicServiceConnection.isConnected.value) return
        val compatState = musicServiceConnection.playbackState.value
        val metadata = musicServiceConnection.currentPlayingSong.value
        
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
            artworkUrl = compat.description.iconUri?.toString()
        )
    }
    
    private fun mapRepeatMode(mode: Int): RepeatMode = when (mode) {
        PlaybackStateCompat.REPEAT_MODE_ONE -> RepeatMode.ONE
        PlaybackStateCompat.REPEAT_MODE_ALL -> RepeatMode.ALL
        else -> RepeatMode.NONE
    }
}
