package com.rld.justlisten.viewmodel.player

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.ui.actions.PlayerAction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.launch

import com.rld.justlisten.media.PlayHistoryTracker

import com.rld.justlisten.datalayer.repositories.AuthRepository

import com.rld.justlisten.datalayer.repositories.PlaylistRepository

import com.rld.justlisten.datalayer.repositories.FeedRepository
import com.rld.justlisten.datalayer.repositories.SettingsRepository
import com.rld.justlisten.datalayer.repositories.SessionState
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

class PlayerViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val libraryRepository: LibraryRepository,
    private val playlistRepository: PlaylistRepository,
    private val musicPlayer: MusicPlayer,
    private val playHistoryTracker: PlayHistoryTracker,
    private val authRepository: AuthRepository,
    private val feedRepository: FeedRepository,
    private val settingsRepository: SettingsRepository,
) : BaseScreenViewModel() {

    private var lastAutoplaySongId: String? = null
    private var fetchDetailsJob: kotlinx.coroutines.Job? = null

    init {
        viewModelScope.launch {
            musicPlayer.currentPlaylist.collect { playlist ->
                val settings = settingsRepository.getSettingsInfo()
                if (settings.isOngoingStreamEnabled && playlist.size == 1) {
                    val singleSongId = playlist.first().id
                    if (singleSongId != lastAutoplaySongId) {
                        lastAutoplaySongId = singleSongId
                        fetchAndAppendRecommendations()
                    }
                }
            }
        }

        viewModelScope.launch {
            var lastTrackId: String? = null
            musicPlayer.playbackState.collect { state ->
                val media = state.currentMedia
                val trackId = media?.id
                if (trackId != null && trackId != lastTrackId) {
                    lastTrackId = trackId
                    fetchDetailsJob?.cancel()
                    fetchDetailsJob = viewModelScope.launch {
                        try {
                            val details = playlistRepository.fetchTrackDetails(trackId)
                            if (details != null) {
                                musicPlayer.updateTrackMetadata(
                                    songId = trackId,
                                    repostCount = details.repostCount,
                                    favoriteCount = details.favoriteCount,
                                    commentCount = details.commentCount,
                                    playCount = details.playCount,
                                    artistId = details.user.id
                                )
                            }
                        } catch (e: Exception) {
                            // Ignore API fetch errors (e.g. offline)
                        }
                    }
                }
            }
        }
    }

    private fun fetchAndAppendRecommendations() {
        viewModelScope.launch {
            try {
                val session = authRepository.sessionState.value
                val recommendedTracks = if (session is SessionState.Authenticated) {
                    feedRepository.getUserFeed(
                        userId = session.userProfile.userId ?: "",
                        limit = 10,
                        tracksOnly = true
                    )
                } else {
                    playlistRepository.getTracks(
                        limit = 10,
                        category = "",
                        timeRange = "week"
                    ).map { PlaylistItem(it._data, it.isFavorite, it.isReposted) }
                }
                
                val currentTrackId = musicPlayer.playbackState.value.currentMedia?.id
                val filteredTracks = recommendedTracks.filter { it.id != currentTrackId }
                
                if (filteredTracks.isNotEmpty()) {
                    musicPlayer.addTracksToQueue(filteredTracks)
                }
            } catch (e: Exception) {
                // Ignore or log error
            }
        }
    }

    private val _addPlaylistList = MutableStateFlow(emptyList<AddPlaylist>())
    val addPlaylistList: StateFlow<List<AddPlaylist>> = _addPlaylistList.asStateFlow()

    private val _showConnectPrompt = MutableStateFlow(false)

    val playerUiState: StateFlow<PlayerUiState> = combine(
        _addPlaylistList,
        musicPlayer.playbackState,
        _showConnectPrompt
    ) { addPlaylists, playbackState, showConnectPrompt ->
        PlayerUiState(
            addPlaylistList = addPlaylists,
            playbackState = playbackState,
            showConnectPrompt = showConnectPrompt
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlayerUiState()
    )

    fun onAction(action: PlayerAction) {
        when (action) {
            is PlayerAction.ToggleFavorite -> {
                saveSongToFavorites(
                    action.songId,
                    action.title,
                    action.user,
                    action.songIcon,
                    action.isFavorite
                )
            }
            is PlayerAction.ToggleRepost -> {
                toggleRepost(action.songId, action.isRepost)
            }
            PlayerAction.DismissConnectPrompt -> {
                _showConnectPrompt.value = false
            }
            is PlayerAction.CreatePlaylist -> {
                savePlaylist(action.name, action.description)
            }
            is PlayerAction.AddSongToPlaylist -> {
                updatePlaylistSongs(action.playlistTitle, action.playlistDescription, action.songs)
            }
            PlayerAction.LoadPlaylists -> {
                loadAddPlaylists()
            }
            PlayerAction.SkipNext -> {
                musicPlayer.skipToNext()
            }
            PlayerAction.SkipPrevious -> {
                musicPlayer.skipToPrevious()
            }
            else -> Unit
        }
    }

    fun loadAddPlaylists() {
        viewModelScope.launch {
            _addPlaylistList.value = libraryRepository.getAddPlaylist()
        }
    }

    fun saveSongToFavorites(
        id: String, title: String, user: UserModel, songIcon: SongIconList, isFavorite: Boolean
    ) {
        viewModelScope.launch {
            favoritesRepository.saveSongToFavorites(id, title, user, songIcon, "Favorite", isFavorite)
            musicPlayer.refreshMetadata()
        }
    }

    fun savePlaylist(name: String, description: String?) {
        viewModelScope.launch {
            libraryRepository.savePlaylist(name, description)
            loadAddPlaylists()
        }
    }

    fun updatePlaylistSongs(title: String, description: String?, songs: List<String>) {
        viewModelScope.launch {
            libraryRepository.updatePlaylistSongs(title, description, songs)
            loadAddPlaylists()
        }
    }

    fun toggleRepost(songId: String, isRepost: Boolean) {
        if (authRepository.sessionState.value is com.rld.justlisten.datalayer.repositories.SessionState.Guest) {
            _showConnectPrompt.value = true
            return
        }
        viewModelScope.launch {
            val success = if (isRepost) {
                playlistRepository.repostTrack(songId)
            } else {
                playlistRepository.unrepostTrack(songId)
            }
            if (success) {
                musicPlayer.refreshMetadata()
            }
        }
    }
}
