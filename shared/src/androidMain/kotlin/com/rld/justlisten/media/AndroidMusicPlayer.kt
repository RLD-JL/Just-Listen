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
    private val _playbackPosition = MutableStateFlow(0L)
    override val playbackPosition: StateFlow<Long> = _playbackPosition.asStateFlow()

    private val _currentPlaylist = MutableStateFlow<List<MediaMetadata>>(emptyList())
    override val currentPlaylist: StateFlow<List<MediaMetadata>> = _currentPlaylist.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _networkError = MutableStateFlow(false)
    override val networkError: StateFlow<Boolean> = _networkError.asStateFlow()

    private val favoriteIds = MutableStateFlow<Set<String>>(emptySet())
    private var positionJob: Job? = null

    private val commands = ReadyCommandQueue(
        scope = scope,
        acquire = {
            ensureConnected()
            musicServiceConnection.awaitController()
        },
        onFailure = { _networkError.value = true },
    )

    private fun ensureConnected() {
        musicServiceConnection.ensureConnected()
        if (positionJob?.isActive == true) return
        positionJob = scope.launch {
            while (isActive) {
                val state = _playbackState.value
                if (state.status == PlaybackStatus.PLAYING && !musicServiceConnection.sliderClicked.value) {
                    _playbackPosition.value =
                        musicServiceConnection.mediaController?.currentPosition ?: 0L
                }
                delay(if (state.status == PlaybackStatus.PLAYING) 250L else 1_000L)
            }
        }
    }

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
                musicServiceConnection.playWhenReady,
                musicServiceConnection.currentPlayingSong,
                musicServiceConnection.currentPlaylist,
                favoriteIds
            ) { state, _, mediaItem, playlist, _ ->
                updateState(state, mediaItem)
                _currentPlaylist.value = playlist.mapNotNull { mapMetadata(it) }
            }.collect()
        }

        scope.launch {
            musicServiceConnection.shuffleModeEnabled.collect {
                refreshMetadata()
            }
        }

        scope.launch {
            musicServiceConnection.repeatMode.collect {
                refreshMetadata()
            }
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

    }

    override fun play() = commands.submit { it.play() }
    override fun pause() = commands.submit { it.pause() }
    override fun stop() = commands.submit { it.stop() }
    override fun skipToNext() = commands.submit { it.seekToNext() }
    override fun skipToPrevious() = commands.submit { it.seekToPrevious() }

    override fun seekTo(position: Long) = commands.submit {
        it.seekTo(position)
        _playbackPosition.value = position
    }

    override fun setShuffleModeEnabled(enabled: Boolean) = commands.submit {
        it.shuffleModeEnabled = enabled
    }

    override fun setRepeatMode(repeatMode: RepeatMode) = commands.submit {
        it.repeatMode = when (repeatMode) {
            RepeatMode.NONE -> Player.REPEAT_MODE_OFF
            RepeatMode.ONE -> Player.REPEAT_MODE_ONE
            RepeatMode.ALL -> Player.REPEAT_MODE_ALL
        }
    }

    override fun playMedia(mediaId: String) = commands.submit { controller ->
        for (i in 0 until controller.mediaItemCount) {
            if (controller.getMediaItemAt(i).mediaId == mediaId) {
                controller.seekTo(i, 0)
                controller.play()
                break
            }
        }
    }

    override fun playMedia(mediaId: String, playlist: List<com.rld.justlisten.viewmodel.interfaces.Item>) =
        commands.submit { controller ->
            val startIndex = playlist.indexOfFirst { it.id == mediaId }.coerceAtLeast(0)
            musicServiceConnection.updatePlaylist(playlist, startIndex)
            controller.play()
        }

    override fun loadMedia(mediaId: String, playlist: List<com.rld.justlisten.viewmodel.interfaces.Item>) =
        commands.submit { controller ->
            val startIndex = playlist.indexOfFirst { it.id == mediaId }.coerceAtLeast(0)
            controller.pause()
            musicServiceConnection.updatePlaylist(playlist, startIndex)
        }

    override fun updatePlaylist(list: List<com.rld.justlisten.viewmodel.interfaces.Item>) =
        commands.submit { musicServiceConnection.updatePlaylist(list) }

    override fun refreshMetadata() {
        updateState(musicServiceConnection.playbackState.value, musicServiceConnection.currentPlayingSong.value)
    }

    override fun updateCurrentTrackRepostState(
        songId: String,
        isReposted: Boolean,
        repostCount: Int,
    ) {
        val currentMedia = playbackState.value.currentMedia
        if (currentMedia?.id == songId) {
            updateTrackMetadata(
                songId = songId,
                isReposted = isReposted,
                repostCount = repostCount,
                favoriteCount = currentMedia.favoriteCount,
                commentCount = currentMedia.commentCount,
                playCount = currentMedia.playCount,
                artistId = currentMedia.artistId,
            )
        }
    }

    override fun updateTrackMetadata(
        songId: String,
        isReposted: Boolean,
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
                is TrackItem -> song.copy(
                    _data = song._data.copy(
                        repostCount = repostCount,
                        favoriteCount = favoriteCount,
                        commentCount = commentCount,
                        playCount = playCount,
                        user = song._data.user.copy(id = artistId)
                    ),
                    isReposted = isReposted,
                    repostCount = repostCount,
                )
                is PlaylistItem -> song.copy(
                    _data = song._data.copy(
                        repostCount = repostCount,
                        favoriteCount = favoriteCount,
                        commentCount = commentCount,
                        playCount = playCount,
                        user = song._data.user.copy(id = artistId)
                    ),
                    isReposted = isReposted,
                    repostCount = repostCount,
                )
                else -> song
            }
            val newList = currentPlaylist.toMutableList()
            newList[songIndex] = updatedSong
            musicServiceConnection.musicSource.playlist = newList
            refreshMetadata()
        }
    }

    override fun removeTrack(index: Int) {
        commands.submit { controller ->
            if (index in 0 until controller.mediaItemCount) {
                controller.removeMediaItem(index)
            }
        }
    }

    override fun moveTrack(fromIndex: Int, toIndex: Int) {
        commands.submit { controller ->
            if (fromIndex in 0 until controller.mediaItemCount && toIndex in 0 until controller.mediaItemCount) {
                controller.moveMediaItem(fromIndex, toIndex)
            }
        }
    }

    override fun addTracksToQueue(tracks: List<com.rld.justlisten.viewmodel.interfaces.Item>) {
        commands.submit { controller ->
            val mediaItems = tracks.map { it.toMediaItem() }
            musicServiceConnection.musicSource.playlist = musicServiceConnection.musicSource.playlist + tracks
            musicServiceConnection.musicSource.songs = musicServiceConnection.musicSource.songs + mediaItems
            controller.addMediaItems(mediaItems)
        }
    }
    
    // Helper to update internal state from MusicServiceConnection
    private fun updateState(state: Int, mediaItem: MediaItem?) {
        if (!musicServiceConnection.isConnected.value) return
        val currentPosition = musicServiceConnection.mediaController?.currentPosition ?: 0L
        _playbackPosition.value = currentPosition
        _playbackState.value = PlaybackState(
            status = mapStatus(state),
            currentPosition = currentPosition,
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
