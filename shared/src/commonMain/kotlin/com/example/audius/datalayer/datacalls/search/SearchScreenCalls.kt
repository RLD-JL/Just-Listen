package com.example.audius.datalayer.datacalls.search

import com.example.audius.datalayer.Repository
import com.example.audius.datalayer.localdb.searchscreen.getSearchInfo
import com.example.audius.datalayer.localdb.searchscreen.saveSearchInfo
import com.example.audius.datalayer.webservices.apis.searchcalls.searchFor
import com.example.audius.viewmodel.screens.search.SearchEnum

fun Repository.saveSearch(search : String) {
    localDb.saveSearchInfo(search)
}

fun Repository.getSearchList() : List<String> {
    return localDb.getSearchInfo()
}

suspend fun Repository.searchForPlaylist(search: String, searchEnum: SearchEnum = SearchEnum.PLAYLIST) : List<String> {
   return searchForTracks(search, searchEnum)
}

suspend fun Repository.searchForTracks(search: String, searchEnum: SearchEnum = SearchEnum.TRACKS) : List<String> {
    return webservices.searchFor(search, searchEnum)?.data?.map { playlistModel ->
        playlistModel.id
    }?.toList() ?: emptyList()
}