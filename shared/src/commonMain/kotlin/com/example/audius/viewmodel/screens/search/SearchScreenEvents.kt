package com.example.audius.viewmodel.screens.search

import com.example.audius.datalayer.datacalls.search.saveSearch
import com.example.audius.datalayer.datacalls.search.searchForPlaylist
import com.example.audius.datalayer.datacalls.search.searchForTracks
import com.example.audius.viewmodel.Events
import com.example.audius.viewmodel.screens.playlist.PlaylistItem
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
        searchState.copy(
            searchResultTracks = tracksList.await(), isLoading = false, searchFor = searchInfo,
            searchResultPlaylist = playList.await()
        )
    }
}

fun Events.updateSearch(searchInfo: String) = screenCoroutine {
    stateManager.updateScreen(SearchScreenState::class) {
        println("update screen=$searchInfo")
        it.copy(searchFor = searchInfo)
    }
}