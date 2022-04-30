package com.example.justlisten.viewmodel.screens.search

import com.example.justlisten.Navigation
import com.example.justlisten.ScreenParams
import com.example.justlisten.datalayer.datacalls.search.getSearchList
import com.example.justlisten.viewmodel.screens.ScreenInitSettings
import kotlinx.serialization.Serializable

fun com.example.justlisten.Navigation.initSearch() = ScreenInitSettings(
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