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
            _playlistDetailState.value = PlaylistDetailState(isLoading = true)
            val enum = runCatching { PlayListEnum.valueOf(args.playlistEnum) }
                .getOrDefault(PlayListEnum.CURRENT_PLAYLIST)
            val currentPlaylist = repository.getPlaylist(
                index = 40,
                enum,
                args.playlistId,
            ).filter { it._data.isStreamable }

            val playlistIcon = if (enum == PlayListEnum.CREATED_BY_USER && currentPlaylist.isNotEmpty()) {
                currentPlaylist[0].songIconList.songImageURL480px
            } else {
                args.playlistIcon
            }

            _playlistDetailState.value = PlaylistDetailState(
                isLoading = false,
                playlistIcon = playlistIcon,
                playListCreatedBy = args.playlistCreatedBy,
                playlistName = args.playlistTitle,
                songPlaylist = currentPlaylist,
            )
        }
    }

    fun popBack() {
        popBackStack()
    }
}
