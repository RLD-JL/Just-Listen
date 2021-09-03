package com.example.audius.viewmodel.screens.trending

import com.example.audius.Navigation
import com.example.audius.ScreenParams
import com.example.audius.datalayer.datacalls.getRemixPlaylist
import com.example.audius.datalayer.datacalls.getTopPlaylist
import com.example.audius.datalayer.datacalls.getTrendingList
import com.example.audius.datalayer.datacalls.getTrendingListData
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
        val listData = dataRepository.getTopPlaylist(index = 20)
        val remixPlaylist = dataRepository.getRemixPlaylist(index = 20)
        stateManager.updateScreen(PlaylistState::class) {
            it.copy(
                remixPlaylist = remixPlaylist,
                isLoading = true,
                playlistItems = listData
            )
        }
    },
    reinitOnEachNavigation = true
)