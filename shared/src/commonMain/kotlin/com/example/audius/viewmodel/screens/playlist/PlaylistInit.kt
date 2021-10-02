package com.example.audius.viewmodel.screens.playlist

import com.example.audius.Navigation
import com.example.audius.ScreenParams
import com.example.audius.datalayer.datacalls.getPlaylist
import com.example.audius.datalayer.datacalls.getTrendingList
import com.example.audius.viewmodel.screens.ScreenInitSettings
import kotlinx.serialization.Serializable

@Serializable
data class TrendingListParams(val string: String) : ScreenParams

fun Navigation.initTrendingList(params: TrendingListParams) = ScreenInitSettings(
    title = "Trending" + params.string,
    initState = { TrendingListState(isLoading = true) },
    callOnInit = {
        val listData = dataRepository.getTrendingList()
        stateManager.updateScreen(TrendingListState::class) {

            it.copy(
                isLoading = false,
                trendingListItems = listData
            )
        }
    },
    reinitOnEachNavigation = true
)

@Serializable
data class PlaylistParams(val string: String) : ScreenParams

fun Navigation.initPlaylist(params: PlaylistParams) = ScreenInitSettings(
    title = "Playlist" + params.string,
    initState = {PlaylistState(isLoading = true)},
    callOnInit = {
        val listData = dataRepository.getPlaylist(index = 20, PlayListEnum.TOP_PLAYLIST)
        val remixPlaylist = dataRepository.getPlaylist(index = 20, PlayListEnum.REMIX)
        val currentPlaylist = dataRepository.getPlaylist(index = 20, PlayListEnum.CURRENT_PLAYLIST)
        stateManager.updateScreen(PlaylistState::class) {
            it.copy(
                remixPlaylist = remixPlaylist,
                isLoading = false,
                playlistItems = listData,
                currentPlaylist = currentPlaylist
            )
        }
    },
    reinitOnEachNavigation = false
)

