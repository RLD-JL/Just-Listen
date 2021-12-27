package com.example.audius.viewmodel.screens.search

import com.example.audius.datalayer.datacalls.search.saveSearch
import com.example.audius.datalayer.datacalls.search.searchFor
import com.example.audius.viewmodel.Events

fun Events.saveSearchInfo(searchInfo: String) = screenCoroutine{
    dataRepository.saveSearch(searchInfo)
}

fun Events.searchFor(searchInfo: String) = screenCoroutine {
    stateManager.updateScreen(SearchScreenState::class) {
        it.copy(listOfSearches = dataRepository.searchFor(searchInfo))
    }
}