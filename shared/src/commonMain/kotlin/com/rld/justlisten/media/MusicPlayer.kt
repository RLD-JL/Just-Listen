package com.rld.justlisten.media

import kotlinx.coroutines.flow.StateFlow

data class MediaMetadata(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val artworkUrl: String? = null,
    val lowResArtworkUrl: String? = null
)

enum class PlaybackStatus {
    IDLE, BUFFERING, PLAYING, PAUSED, STOPPED, ERROR
}

data class PlaybackState(
    val status: PlaybackStatus,
    val currentPosition: Long,
    val currentMedia: MediaMetadata? = null,
    val isShuffleModeEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.NONE
)

enum class RepeatMode {
    NONE, ONE, ALL
}

interface MusicPlayer {
    val playbackState: StateFlow<PlaybackState>
    val isConnected: StateFlow<Boolean>
    val networkError: StateFlow<Boolean>
    
    fun play()
    fun pause()
    fun stop()
    fun skipToNext()
    fun skipToPrevious()
    fun seekTo(position: Long)
    fun setShuffleModeEnabled(enabled: Boolean)
    fun setRepeatMode(repeatMode: RepeatMode)
    fun playMedia(mediaId: String)
    fun updatePlaylist(list: List<com.rld.justlisten.viewmodel.interfaces.Item>)
}
