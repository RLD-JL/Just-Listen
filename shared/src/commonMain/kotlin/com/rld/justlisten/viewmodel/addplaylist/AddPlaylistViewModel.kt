package com.rld.justlisten.viewmodel.addplaylist

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.addplaylist.AddPlaylistState
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddPlaylistViewModel(
    private val libraryRepository: LibraryRepository,
) : BaseScreenViewModel() {

    private val _addPlaylistState = MutableStateFlow(AddPlaylistState(isLoading = true))
    val addPlaylistState: StateFlow<AddPlaylistState> = _addPlaylistState.asStateFlow()

    init {
        refreshPlaylists()
    }

    fun refreshPlaylists() {
        viewModelScope.launch {
            val playlists = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                libraryRepository.getAddPlaylist()
            }
            _addPlaylistState.update {
                it.copy(isLoading = false, playlistsCreated = playlists)
            }
        }
    }

    fun onAddPlaylistClicked(name: String, description: String?) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            libraryRepository.savePlaylist(name, description)
            refreshPlaylists()
        }
    }

    fun onPlaylistItemClicked(title: String, description: String?, songs: List<String>) {
        navigate(
            Route.PlaylistDetail(
                playlistId = "",
                playlistIcon = "",
                playlistTitle = title,
                playlistCreatedBy = "ME",
                playlistEnum = "CREATED_BY_USER",
                songsList = songs
            ),
        )
    }

    fun popBack() {
        popBackStack()
    }

    fun updatePlaylistSongs(title: String, description: String?, songs: List<String>) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            libraryRepository.updatePlaylistSongs(title, description, songs)
        }
    }
}
