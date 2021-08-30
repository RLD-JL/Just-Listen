package com.example.audius.viewmodel.screens.trending

import com.example.audius.Navigation
import com.example.audius.ScreenParams
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