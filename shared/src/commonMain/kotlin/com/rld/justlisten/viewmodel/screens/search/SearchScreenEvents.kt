package com.rld.justlisten.viewmodel.screens.search

import com.rld.justlisten.datalayer.datacalls.search.saveSearch
import com.rld.justlisten.datalayer.datacalls.search.searchForPlaylist
import com.rld.justlisten.datalayer.datacalls.search.searchForTracks
import com.rld.justlisten.viewmodel.Events
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

fun Events.saveSearchInfo(searchInfo: String) = screenCoroutine {
    dataRepository.saveSearch(searchInfo)
}

fun Events.searchFor(searchInfo: String) = screenCoroutine {
    stateManager.updateScreen(SearchScreenState::class) {
        it.copy(isLoading = true)
    }
    stateManager.updateScreen(SearchScreenState::class) { searchState ->
        val tracksList: Deferred<List<TrackItem>>
        val playList: Deferred<List<PlaylistItem>>
        coroutineScope {
            tracksList = async { dataRepository.searchForTracks(searchInfo) }
            playList = async { dataRepository.searchForPlaylist(searchInfo) }
        }
        val trackListResult = tracksList.await()
        val playlistResult = playList.await()
        searchState.copy(
            searchResultTracks = trackListResult, isLoading = false, searchFor = searchInfo,
            searchResultPlaylist = playlistResult
        )
    }
}

fun Events.updateSearch(searchInfo: String) = screenCoroutine {
    stateManager.updateScreen(SearchScreenState::class) {
        it.copy(searchFor = searchInfo)
    }
}