package com.example.audius.viewmodel.screens.search

import com.example.audius.ScreenState
import com.example.audius.viewmodel.screens.playlist.PlaylistItem

data class SearchScreenState(
    var isLoading: Boolean = false,
    val searchFor: String = "",
    var listOfSearches: List<String> = emptyList(),
    var searchResultTracks: List<String> = emptyList(),
    var searchResultPlaylist: List<String> = emptyList()
) : ScreenState
