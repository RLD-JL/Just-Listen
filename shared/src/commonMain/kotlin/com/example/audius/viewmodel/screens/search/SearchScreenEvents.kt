package com.example.audius.viewmodel.screens.search

import com.example.audius.datalayer.datacalls.search.saveSearch
import com.example.audius.datalayer.datacalls.search.searchForPlaylist
import com.example.audius.datalayer.datacalls.search.searchForTracks
import com.example.audius.viewmodel.Events

fun Events.saveSearchInfo(searchInfo: String) = screenCoroutine{
    dataRepository.saveSearch(searchInfo)
}

fun Events.searchFor(searchInfo: String) = screenCoroutine {
    stateManager.updateScreen(SearchScreenState::class) {
        it.copy(isLoading = true)
    }
    stateManager.updateScreen(SearchScreenState::class) { searchState ->
        val tracksList = dataRepository.searchForTracks(searchInfo)
        val playList = dataRepository.searchForPlaylist(searchInfo)
        searchState.copy(searchResultTracks = tracksList, isLoading = false, searchFor = searchInfo,
        searchResultPlaylist = playList)
    }
}

fun Events.updateSearch(searchInfo: String) = screenCoroutine {
    stateManager.updateScreen(SearchScreenState::class) {
        println("update screen=$searchInfo")
        it.copy(searchFor = searchInfo)
    }
}