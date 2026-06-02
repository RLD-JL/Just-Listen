package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.localdb.searchscreen.getSearchInfo
import com.rld.justlisten.datalayer.localdb.searchscreen.saveSearchInfo
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.searchcalls.searchFor
import com.rld.justlisten.datalayer.webservices.apis.searchcalls.searchAutocomplete
import com.rld.justlisten.datalayer.webservices.apis.searchcalls.AutocompleteUser
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.screens.search.SearchEnum
import com.rld.justlisten.viewmodel.screens.search.TrackItem

data class AutocompleteSuggestions(
    val tracks: List<TrackItem> = emptyList(),
    val playlists: List<PlaylistItem> = emptyList(),
    val users: List<AutocompleteUser> = emptyList()
)

interface SearchRepository {
    fun saveSearch(search: String)
    fun getSearchList(): List<String>
    suspend fun searchForPlaylist(search: String, searchEnum: SearchEnum = SearchEnum.PLAYLIST): List<PlaylistItem>
    suspend fun searchForTracks(search: String, searchEnum: SearchEnum = SearchEnum.TRACKS): List<TrackItem>
    suspend fun searchAutocomplete(query: String): AutocompleteSuggestions
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

    override suspend fun searchAutocomplete(query: String): AutocompleteSuggestions {
        val autocompleteData = webservices.searchAutocomplete(query)?.data ?: return AutocompleteSuggestions()
        val tracks = autocompleteData.tracks.filter { it.isStreamable }.map { TrackItem(it) }
        val playlistItems = autocompleteData.playlists.map { PlaylistItem(it) }
        val albumItems = autocompleteData.albums.map { PlaylistItem(it) }
        val combinedPlaylists = playlistItems + albumItems
        return AutocompleteSuggestions(
            tracks = tracks,
            playlists = combinedPlaylists,
            users = autocompleteData.users
        )
    }
}

