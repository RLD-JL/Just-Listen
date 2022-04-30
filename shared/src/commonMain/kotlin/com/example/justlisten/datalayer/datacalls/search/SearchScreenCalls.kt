package com.example.justlisten.datalayer.datacalls.search

import com.example.justlisten.datalayer.Repository
import com.example.justlisten.datalayer.localdb.searchscreen.getSearchInfo
import com.example.justlisten.datalayer.localdb.searchscreen.saveSearchInfo
import com.example.justlisten.datalayer.models.PlayListModel
import com.example.justlisten.datalayer.webservices.apis.searchcalls.searchFor
import com.example.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.example.justlisten.viewmodel.screens.search.SearchEnum
import com.example.justlisten.viewmodel.screens.search.TrackItem

fun Repository.saveSearch(search : String) {
    localDb.saveSearchInfo(search)
}

fun Repository.getSearchList() : List<String> {
    return localDb.getSearchInfo()
}

suspend fun Repository.searchForPlaylist(search: String, searchEnum: SearchEnum = SearchEnum.PLAYLIST) : List<PlaylistItem> {
    return webservices.searchFor(search, searchEnum)?.data?.map { playlistModel ->
        PlaylistItem(playlistModel)
    }?.toList() ?: emptyList()
}

suspend fun Repository.searchForTracks(search: String, searchEnum: SearchEnum = SearchEnum.TRACKS) : List<TrackItem> {
    return webservices.searchFor(search, searchEnum)?.data?.map { playlistModel ->
        TrackItem(playlistModel)
    }?.toList() ?: emptyList()
}