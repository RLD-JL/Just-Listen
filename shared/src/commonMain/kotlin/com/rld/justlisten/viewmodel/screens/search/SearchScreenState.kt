package com.rld.justlisten.viewmodel.screens.search

import com.rld.justlisten.ScreenState
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.webservices.apis.searchcalls.AutocompleteUser
import com.rld.justlisten.viewmodel.interfaces.Item
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

enum class SearchSeeAllType {
    NONE,
    ARTISTS,
    SONGS,
    PLAYLISTS
}

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
    var autocompleteUsers: List<AutocompleteUser> = emptyList(),

    // See All paginated states
    var seeAllType: SearchSeeAllType = SearchSeeAllType.NONE,
    var seeAllTracks: List<TrackItem> = emptyList(),
    var seeAllPlaylists: List<PlaylistItem> = emptyList(),
    var seeAllUsers: List<AutocompleteUser> = emptyList(),
    var isSeeAllLoading: Boolean = false,
    var seeAllOffset: Int = 0,
    var seeAllLastItemReached: Boolean = false
) : ScreenState


data class TrackItem(
    val _data: PlayListModel,
    override var isFavorite: Boolean = false,
    override var isReposted: Boolean = _data.hasCurrentUserReposted
) : Item {
    override val user = _data.user.username
    override val title = _data.title
    override val playlistTitle = _data.playlistTitle
    override val id = _data.id
    override val songIconList = _data.songImgList
    override val songCounter: String
        get() = _data.songCounter
    override val repostCount = _data.repostCount
    override val favoriteCount = _data.favoriteCount
    override val commentCount = _data.commentCount
    override val playCount = if (_data.isPlaylist) _data.totalPlayCount else _data.playCount
    override val duration = _data.duration
    override val userId = _data.user.id
}