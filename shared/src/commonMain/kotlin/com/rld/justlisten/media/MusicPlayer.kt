package com.rld.justlisten.media

import kotlinx.coroutines.flow.StateFlow

data class MediaMetadata(
    val id: String,
    val title: String,
    val artist: String,
    val duration: Long,
    val artworkUrl: String? = null,
    val lowResArtworkUrl: String? = null,
    val isFavorite: Boolean = false,
    val isReposted: Boolean = false,
    val repostCount: Int = 0,
    val favoriteCount: Int = 0,
    val commentCount: Int = 0,
    val playCount: Int = 0,
    val artistId: String = ""
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
    val currentPlaylist: StateFlow<List<MediaMetadata>>
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
    fun refreshMetadata()
    fun updateTrackMetadata(
        songId: String,
        repostCount: Int,
        favoriteCount: Int,
        commentCount: Int,
        playCount: Int,
        artistId: String
    )
    fun removeTrack(index: Int)
    fun moveTrack(fromIndex: Int, toIndex: Int)
    var currentlyPlayingPlaylistId: String?
    fun addTracksToQueue(tracks: List<com.rld.justlisten.viewmodel.interfaces.Item>)
    fun release()
}
