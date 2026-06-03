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

class PlayerViewModel(
    private val favoritesRepository: FavoritesRepository,
    private val libraryRepository: LibraryRepository,
    private val musicPlayer: MusicPlayer,
    private val playHistoryTracker: PlayHistoryTracker,
) : BaseScreenViewModel() {

    private val _addPlaylistList = MutableStateFlow(emptyList<AddPlaylist>())
    val addPlaylistList: StateFlow<List<AddPlaylist>> = _addPlaylistList.asStateFlow()

    val playerUiState: StateFlow<PlayerUiState> = combine(
        _addPlaylistList,
        musicPlayer.playbackState
    ) { addPlaylists, playbackState ->
        PlayerUiState(
            addPlaylistList = addPlaylists,
            playbackState = playbackState
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
}
