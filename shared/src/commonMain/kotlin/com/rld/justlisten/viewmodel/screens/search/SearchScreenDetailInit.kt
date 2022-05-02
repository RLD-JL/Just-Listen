package com.rld.justlisten.viewmodel.screens.search

import com.rld.justlisten.Navigation
import com.rld.justlisten.ScreenParams
import com.rld.justlisten.datalayer.datacalls.search.getSearchList
import com.rld.justlisten.viewmodel.screens.ScreenInitSettings
import kotlinx.serialization.Serializable

fun Navigation.initSearch() = ScreenInitSettings(
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
    reinitOnEachNavigation = false
)