package com.rld.justlisten.viewmodel.screens.search

import com.rld.justlisten.ScreenState
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.webservices.apis.searchcalls.AutocompleteUser
import com.rld.justlisten.viewmodel.interfaces.Item
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

data class SearchScreenState(
    var isLoading: Boolean = false,
    var searchFor: String = "",
    var listOfSearches: List<String> = emptyList(),
    var searchResultTracks: List<TrackItem> = emptyList(),
    var searchResultPlaylist: List<PlaylistItem> = emptyList(),
    var searchResultUsers: List<AutocompleteUser> = emptyList(),
    
    // Autocomplete suggestion states
    var isAutocompleteLoading: Boolean = false,
    var autocompleteTracks: List<TrackItem> = emptyList(),
    var autocompletePlaylists: List<PlaylistItem> = emptyList(),
    var autocompleteUsers: List<AutocompleteUser> = emptyList()
) : ScreenState


data class TrackItem(val _data: PlayListModel, override var isFavorite: Boolean = false) : Item {
    override val user = _data.user.username
    override val title = _data.title
    override val playlistTitle = _data.playlistTitle
    override val id = _data.id
    override val songIconList = _data.songImgList
    override val songCounter: String
        get() = _data.songCounter
    override val repostCount = _data.repostCount
    override val favoriteCount = _data.favoriteCount
}