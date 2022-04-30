package com.rld.justlisten.datalayer.datacalls.search

import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.localdb.searchscreen.getSearchInfo
import com.rld.justlisten.datalayer.localdb.searchscreen.saveSearchInfo
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.webservices.apis.searchcalls.searchFor
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.screens.search.SearchEnum
import com.rld.justlisten.viewmodel.screens.search.TrackItem

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