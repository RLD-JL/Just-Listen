package com.rld.justlisten.viewmodel.screens.search

import com.rld.justlisten.ScreenState
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.webservices.apis.searchcalls.AutocompleteUser
import com.rld.justlisten.viewmodel.interfaces.Item
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import androidx.compose.runtime.Immutable

enum class SearchSeeAllType {
    NONE,
    ARTISTS,
    SONGS,
    PLAYLISTS
}


@Immutable
data class SearchScreenState(
    val isLoading: Boolean = false,
    val searchFor: String = "",
    val listOfSearches: List<String> = emptyList(),
    val searchResultTracks: List<TrackItem> = emptyList(),
    val searchResultPlaylist: List<PlaylistItem> = emptyList(),
    val searchResultUsers: List<AutocompleteUser> = emptyList(),
    
    // Autocomplete suggestion states
    val isAutocompleteLoading: Boolean = false,
    val autocompleteTracks: List<TrackItem> = emptyList(),
    val autocompletePlaylists: List<PlaylistItem> = emptyList(),
    val autocompleteUsers: List<AutocompleteUser> = emptyList(),

    // See All paginated states
    val seeAllType: SearchSeeAllType = SearchSeeAllType.NONE,
    val seeAllTracks: List<TrackItem> = emptyList(),
    val seeAllPlaylists: List<PlaylistItem> = emptyList(),
    val seeAllUsers: List<AutocompleteUser> = emptyList(),
    val isSeeAllLoading: Boolean = false,
    val seeAllOffset: Int = 0,
    val seeAllLastItemReached: Boolean = false
) : ScreenState


data class TrackItem(
    val _data: PlayListModel,
    override val isFavorite: Boolean = false,
    override val isReposted: Boolean = _data.hasCurrentUserReposted,
    override val repostCount: Int = _data.repostCount,
    override val favoriteCount: Int = _data.favoriteCount
) : Item {
    override val user = _data.user.username
    override val title = _data.title
    override val playlistTitle = _data.playlistTitle
    override val id = _data.id
    override val songIconList = _data.songImgList
    override val songCounter: String
        get() = _data.songCounter
    override val commentCount = _data.commentCount
    override val playCount = if (_data.isPlaylist) _data.totalPlayCount else _data.playCount
    override val duration = _data.duration
    override val userId = _data.user.id
}