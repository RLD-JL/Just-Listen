package com.rld.justlisten.media

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.rld.justlisten.viewmodel.interfaces.Item

/**
 * iOS stub implementation of MusicPlayer
 * To be implemented with platform-specific audio framework
 */
class IOSMusicPlayer : MusicPlayer {

    private val _playbackState = MutableStateFlow(
        PlaybackState(
            status = PlaybackStatus.IDLE,
            currentPosition = 0L,
            currentMedia = null,
            isShuffleModeEnabled = false,
            repeatMode = RepeatMode.NONE
        )
    )
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<MediaMetadata>>(emptyList())
    override val currentPlaylist: StateFlow<List<MediaMetadata>> = _currentPlaylist.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _networkError = MutableStateFlow(false)
    override val networkError: StateFlow<Boolean> = _networkError.asStateFlow()

    override fun play() {
        // TODO: Implement iOS audio playback
    }

    override fun pause() {
        // TODO: Implement iOS audio pause
    }

    override fun stop() {
        // TODO: Implement iOS audio stop
    }

    override fun skipToNext() {
        // TODO: Implement iOS skip next
    }

    override fun skipToPrevious() {
        // TODO: Implement iOS skip previous
    }

    override fun seekTo(position: Long) {
        // TODO: Implement iOS seek
    }

    override fun setShuffleModeEnabled(enabled: Boolean) {
        // TODO: Implement iOS shuffle mode
    }

    override fun setRepeatMode(repeatMode: RepeatMode) {
        // TODO: Implement iOS repeat mode
    }

    override fun playMedia(mediaId: String) {
        // TODO: Implement iOS play media
    }

    override fun updatePlaylist(list: List<Item>) {
        // TODO: Implement iOS update playlist
    }

    override fun removeTrack(index: Int) {
        // TODO: Implement iOS remove track
    }

    override fun moveTrack(fromIndex: Int, toIndex: Int) {
        // TODO: Implement iOS move track
    }
}

