package com.example.audius.viewmodel.screens.search

import com.example.audius.Navigation
import com.example.audius.ScreenParams
import com.example.audius.datalayer.datacalls.search.getSearchList
import com.example.audius.viewmodel.screens.ScreenInitSettings
import kotlinx.serialization.Serializable

@Serializable
data class SearchInitParams(val search: String) : ScreenParams

fun Navigation.initSearch(params: ScreenParams) = ScreenInitSettings(
    title = "Search",
    initState = { SearchScreenState(isLoading = true) },
    callOnInit = {
                 val listOfSearches = dataRepository.getSearchList()
        stateManager.updateScreen(SearchScreenState::class) {
            it.copy(isLoading = false,
            searchFor = "",
            listOfSearches = listOfSearches)
        }

    },
    reinitOnEachNavigation = true
)