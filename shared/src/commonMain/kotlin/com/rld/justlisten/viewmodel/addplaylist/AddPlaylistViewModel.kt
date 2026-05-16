package com.rld.justlisten.viewmodel.addplaylist

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.datacalls.addplaylistscreen.getAddPlaylist
import com.rld.justlisten.datalayer.datacalls.addplaylistscreen.savePlaylist
import com.rld.justlisten.datalayer.datacalls.addplaylistscreen.updatePlaylistSongs
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.addplaylist.AddPlaylistState
import com.rld.justlisten.viewmodel.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AddPlaylistViewModel(
    private val repository: Repository,
) : BaseScreenViewModel() {

    private val _addPlaylistState = MutableStateFlow(AddPlaylistState(isLoading = true))
    val addPlaylistState: StateFlow<AddPlaylistState> = _addPlaylistState.asStateFlow()

    init {
        refreshPlaylists()
    }

    fun refreshPlaylists() {
        viewModelScope.launch {
            _addPlaylistState.update {
                it.copy(isLoading = false, playlistsCreated = repository.getAddPlaylist())
            }
        }
    }

    fun onAddPlaylistClicked(name: String, description: String?) {
        repository.savePlaylist(name, description)
        refreshPlaylists()
    }

    fun onPlaylistItemClicked(title: String, description: String?, songs: List<String>) {
        navigate(
            Route.PlaylistDetail(
                playlistId = "",
                playlistIcon = "",
                playlistTitle = title,
                playlistCreatedBy = "ME",
                playlistEnum = "CREATED_BY_USER",
            ),
        )
    }

    fun popBack() {
        popBackStack()
    }

    fun updatePlaylistSongs(title: String, description: String?, songs: List<String>) {
        repository.updatePlaylistSongs(title, description, songs)
    }
}
