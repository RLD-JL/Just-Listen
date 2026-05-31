package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.localdb.searchscreen.getSearchInfo
import com.rld.justlisten.datalayer.localdb.searchscreen.saveSearchInfo
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.searchcalls.searchFor
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.screens.search.SearchEnum
import com.rld.justlisten.viewmodel.screens.search.TrackItem

interface SearchRepository {
    fun saveSearch(search: String)
    fun getSearchList(): List<String>
    suspend fun searchForPlaylist(search: String, searchEnum: SearchEnum = SearchEnum.PLAYLIST): List<PlaylistItem>
    suspend fun searchForTracks(search: String, searchEnum: SearchEnum = SearchEnum.TRACKS): List<TrackItem>
}

class SearchRepositoryImpl(
    private val localDb: LocalDb,
    private val webservices: ApiClient
) : SearchRepository {

    override fun saveSearch(search: String) {
        localDb.saveSearchInfo(search)
    }

    override fun getSearchList(): List<String> {
        return localDb.getSearchInfo()
    }

    override suspend fun searchForPlaylist(
        search: String,
        searchEnum: SearchEnum
    ): List<PlaylistItem> {
        return webservices.searchFor(search, searchEnum)?.data?.map { playlistModel ->
            PlaylistItem(playlistModel)
        }?.toList() ?: emptyList()
    }

    override suspend fun searchForTracks(
        search: String,
        searchEnum: SearchEnum
    ): List<TrackItem> {
        return webservices.searchFor(search, searchEnum)?.data?.map { playlistModel ->
            TrackItem(playlistModel)
        }?.toList() ?: emptyList()
    }
}
