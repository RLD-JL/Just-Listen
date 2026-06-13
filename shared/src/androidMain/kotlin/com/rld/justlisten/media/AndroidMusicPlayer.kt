package com.rld.justlisten.media

import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.rld.justlisten.media.exoplayer.MusicServiceConnection
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

import com.rld.justlisten.datalayer.repositories.PlaylistRepository
import com.rld.justlisten.viewmodel.screens.search.TrackItem
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

class AndroidMusicPlayer(
    val musicServiceConnection: MusicServiceConnection,
    private val favoritesRepository: FavoritesRepository,
    private val playlistRepository: PlaylistRepository
) : MusicPlayer {

    override var currentlyPlayingPlaylistId: String? = null

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val _playbackState = MutableStateFlow(PlaybackState(PlaybackStatus.IDLE, 0L))
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<MediaMetadata>>(emptyList())
    override val currentPlaylist: StateFlow<List<MediaMetadata>> = _currentPlaylist.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _networkError = MutableStateFlow(false)
    override val networkError: StateFlow<Boolean> = _networkError.asStateFlow()

    private val favoriteIds = MutableStateFlow<Set<String>>(emptySet())

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
                musicServiceConnection.currentPlaylist,
                favoriteIds
            ) { state, mediaItem, playlist, _ ->
                updateState(state, mediaItem)
                _currentPlaylist.value = playlist.mapNotNull { mapMetadata(it) }
            }.collect()
        }

        scope.launch {
            favoritesRepository.getFavoritePlaylistFlow().collect { favoriteList ->
                val ids = favoriteList.map { it.id }.toSet()
                favoriteIds.value = ids
                if (ids.contains(_playbackState.value.currentMedia?.id)) {
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

    override fun updateTrackMetadata(
        songId: String,
        repostCount: Int,
        favoriteCount: Int,
        commentCount: Int,
        playCount: Int,
        artistId: String
    ) {
        val currentPlaylist = musicServiceConnection.musicSource.playlist
        val songIndex = currentPlaylist.indexOfFirst { it.id == songId }
        if (songIndex != -1) {
            val song = currentPlaylist[songIndex]
            val updatedSong = when (song) {
                is TrackItem -> song.copy(_data = song._data.copy(
                    repostCount = repostCount,
                    favoriteCount = favoriteCount,
                    commentCount = commentCount,
                    playCount = playCount,
                    user = song._data.user.copy(id = artistId)
                ))
                is PlaylistItem -> song.copy(_data = song._data.copy(
                    repostCount = repostCount,
                    favoriteCount = favoriteCount,
                    commentCount = commentCount,
                    playCount = playCount,
                    user = song._data.user.copy(id = artistId)
                ))
                else -> song
            }
            val newList = currentPlaylist.toMutableList()
            newList[songIndex] = updatedSong
            musicServiceConnection.musicSource.playlist = newList
            refreshMetadata()
        }
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

    override fun addTracksToQueue(tracks: List<com.rld.justlisten.viewmodel.interfaces.Item>) {
        val mediaItems = tracks.map { it.toMediaItem() }
        musicServiceConnection.musicSource.playlist = musicServiceConnection.musicSource.playlist + tracks
        musicServiceConnection.musicSource.songs = musicServiceConnection.musicSource.songs + mediaItems
        musicServiceConnection.mediaController?.addMediaItems(mediaItems)
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
        val originalSong = musicServiceConnection.musicSource.playlist.find { it.id == id }
        return MediaMetadata(
            id = id,
            title = metadata.title?.toString() ?: "",
            artist = metadata.artist?.toString() ?: "",
            duration = musicServiceConnection.mediaController?.duration ?: 0L,
            artworkUrl = metadata.artworkUri?.toString(),
            lowResArtworkUrl = metadata.artworkUri?.toString(),
            isFavorite = favoriteIds.value.contains(id),
            isReposted = playlistRepository.isTrackReposted(id),
            repostCount = originalSong?.repostCount ?: 0,
            favoriteCount = originalSong?.favoriteCount ?: 0,
            commentCount = originalSong?.commentCount ?: 0,
            playCount = originalSong?.playCount ?: 0,
            artistId = originalSong?.userId ?: ""
        )
    }
    
    private fun mapRepeatMode(mode: Int): RepeatMode = when (mode) {
        Player.REPEAT_MODE_ONE -> RepeatMode.ONE
        Player.REPEAT_MODE_ALL -> RepeatMode.ALL
        else -> RepeatMode.NONE
    }

    override fun release() {
        scope.cancel()
        musicServiceConnection.release()
    }
}
