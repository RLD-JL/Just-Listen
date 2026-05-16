package com.rld.justlisten.viewmodel.playlist

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.datacalls.playlist.getPlaylist
import com.rld.justlisten.datalayer.datacalls.playlist.getTracks
import com.rld.justlisten.datalayer.utils.Constants.list
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistState
import com.rld.justlisten.viewmodel.screens.playlist.TimeRange
import com.rld.justlisten.viewmodel.screens.playlist.TracksCategory
import com.rld.justlisten.viewmodel.update
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

class PlaylistViewModel(
    private val repository: Repository,
) : BaseScreenViewModel() {

    private val _playlistState = MutableStateFlow(PlaylistState(isLoading = true))
    val playlistState: StateFlow<PlaylistState> = _playlistState.asStateFlow()

    var currentPlaylistId: String = ""

    init {
        refreshScreen()
    }

    fun refreshScreen() {
        viewModelScope.launch {
            _playlistState.update { it.copy(isLoading = true) }
            var queryIndex = Random.nextInt(0, list.size)
            val queryIndex2 = Random.nextInt(0, list.size).let {
                if (it == queryIndex) if (it > 0) it - 1 else it + 1 else it
            }
            coroutineScope {
                val top = async { repository.getPlaylist(20, PlayListEnum.TOP_PLAYLIST) }
                val remix = async {
                    repository.getPlaylist(20, PlayListEnum.REMIX, queryPlaylist = list[queryIndex])
                }
                val hot = async {
                    repository.getPlaylist(20, PlayListEnum.HOT, queryPlaylist = list[queryIndex2])
                }
                _playlistState.update {
                    it.copy(
                        remixPlaylist = remix.await().shuffled(),
                        isLoading = false,
                        queryIndex = queryIndex,
                        queryIndex2 = queryIndex2,
                        playlistItems = top.await().shuffled(),
                        hotPlaylist = hot.await().shuffled(),
                    )
                }
            }
        }
    }

    fun fetchPlaylist(index: Int, playlistEnum: PlayListEnum, queryPlaylist: String = "Rock") {
        viewModelScope.launch {
            _playlistState.update { state ->
                when (playlistEnum) {
                    PlayListEnum.TOP_PLAYLIST -> {
                        val items = repository.getPlaylist(index, playlistEnum)
                        if (items.size == state.playlistItems.size) {
                            state.copy(lastFetchPlaylist = true)
                        } else {
                            state.copy(playlistItems = items)
                        }
                    }
                    PlayListEnum.REMIX -> {
                        val items = repository.getPlaylist(index, playlistEnum, queryPlaylist)
                        if (items.size == state.remixPlaylist.size) {
                            state.copy(lastFetchRemix = true)
                        } else {
                            state.copy(remixPlaylist = items)
                        }
                    }
                    PlayListEnum.HOT -> {
                        val items = repository.getPlaylist(index, playlistEnum, queryPlaylist)
                        if (items.size == state.hotPlaylist.size) {
                            state.copy(lastFetchHot = true)
                        } else {
                            state.copy(hotPlaylist = items)
                        }
                    }
                    else -> state
                }
            }
        }
    }

    fun getNewTracks(category: TracksCategory, timeRange: TimeRange) {
        viewModelScope.launch {
            _playlistState.update { it.copy(tracksLoading = true) }
            val time = when (timeRange) {
                TimeRange.ALLTIME -> "allTime"
                TimeRange.MONTH -> TimeRange.MONTH.value.lowercase()
                TimeRange.WEEK -> TimeRange.WEEK.value.lowercase()
            }
            val searchCategory = if (category == TracksCategory.RAP) "Hip-Hop/Rap" else category.value
            val tracks = repository.getTracks(16, searchCategory, time)
            _playlistState.update { it.copy(tracksList = tracks, tracksLoading = false) }
        }
    }

    fun onPlaylistClicked(
        playlistId: String,
        playlistIcon: String,
        playlistCreatedBy: String,
        playlistTitle: String,
    ) {
        currentPlaylistId = playlistId
        navigate(
            Route.PlaylistDetail(
                playlistId = playlistId,
                playlistIcon = playlistIcon,
                playlistTitle = playlistTitle,
                playlistCreatedBy = playlistCreatedBy,
                playlistEnum = "CURRENT_PLAYLIST",
            ),
        )
    }

    fun onSearchClicked() {
        navigate(Route.Search)
    }
}
