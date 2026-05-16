package com.rld.justlisten.viewmodel.playlistdetail

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.datacalls.playlist.getPlaylist
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState
import com.rld.justlisten.viewmodel.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(
    private val repository: Repository,
) : BaseScreenViewModel() {

    private val _playlistDetailState = MutableStateFlow(PlaylistDetailState(isLoading = true))
    val playlistDetailState: StateFlow<PlaylistDetailState> = _playlistDetailState.asStateFlow()

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

    fun popBack() {
        popBackStack()
    }
}
