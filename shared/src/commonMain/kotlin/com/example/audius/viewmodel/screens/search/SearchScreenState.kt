package com.example.audius.viewmodel.screens.search

import com.example.audius.ScreenState
import com.example.audius.datalayer.models.PlayListModel
import com.example.audius.datalayer.models.SongIconList
import com.example.audius.viewmodel.interfaces.Item
import com.example.audius.viewmodel.screens.playlist.PlaylistItem

data class SearchScreenState(
    var isLoading: Boolean = false,
    var searchFor: String = "",
    var listOfSearches: List<String> = emptyList(),
    var searchResultTracks: List<TrackItem> = emptyList(),
    var searchResultPlaylist: List<PlaylistItem> = emptyList()
) : ScreenState


data class TrackItem(val _data: PlayListModel) : Item {
    override val user = _data.user.username
    override val title = _data.title
    override val playlistTitle = _data.playlistTitle
    override val id = _data.id
    override val songIconList = _data.songImgList
}