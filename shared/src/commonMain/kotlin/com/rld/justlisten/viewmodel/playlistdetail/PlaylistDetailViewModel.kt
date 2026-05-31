package com.rld.justlisten.viewmodel.playlistdetail

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.datacalls.addplaylistscreen.deletePlaylist
import com.rld.justlisten.datalayer.datacalls.playlist.getPlaylist
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState
import kotlinx.coroutines.flow.update
import com.rld.justlisten.datalayer.datacalls.library.saveSongToFavorites
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(
    private val repository: Repository,
) : BaseScreenViewModel() {

    private val _playlistDetailState = MutableStateFlow(PlaylistDetailState(isLoading = true))
    val playlistDetailState: StateFlow<PlaylistDetailState> = _playlistDetailState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getFavoritePlaylistFlow().collect { favoriteList ->
                val favoriteIds = favoriteList.map { it.id }.toSet()
                _playlistDetailState.update { state ->
                    val updated = state.songPlaylist.map { item ->
                        item.copy(isFavorite = favoriteIds.contains(item.id))
                    }
                    state.copy(songPlaylist = updated)
                }
            }
        }
    }

    fun onFavoritePressed(
        id: String, title: String, user: UserModel, songIconList: SongIconList,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            repository.saveSongToFavorites(id, title, user, songIconList, "Favorite", isFavorite)
            // Update in-memory state so UI reflects the change immediately
            _playlistDetailState.update { state ->
                state.copy(
                    songPlaylist = state.songPlaylist.map { item ->
                        if (item.id == id) {
                            item.copy(isFavorite = isFavorite)
                        } else {
                            item
                        }
                    }
                )
            }
        }
    }

    fun load(args: Route.PlaylistDetail) {
        viewModelScope.launch {
            _playlistDetailState.update { 
                it.copy(
                    isLoading = true, 
                    playlistName = args.playlistTitle, 
                    playListCreatedBy = args.playlistCreatedBy, 
                    playlistIcon = args.playlistIcon
                ) 
            }
            val playlistEnum = PlayListEnum.valueOf(args.playlistEnum)
            val songs = repository.getPlaylist(
                index = 0, 
                playListEnum = playlistEnum, 
                playlistId = args.playlistId,
                songsList = args.songsList
            )
            _playlistDetailState.update {
                it.copy(
                    isLoading = false,
                    songPlaylist = songs,
                )
            }
        }
    }

    fun deletePlaylist(playlistName: String) {
        viewModelScope.launch {
            repository.deletePlaylist(playlistName)
            popBackStack()
        }
    }

    fun popBack() {
        popBackStack()
    }
}
