package com.rld.justlisten.viewmodel.screens.search

import com.rld.justlisten.ScreenState
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.viewmodel.interfaces.Item
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

data class SearchScreenState(
    var isLoading: Boolean = false,
    var searchFor: String = "",
    var listOfSearches: List<String> = emptyList(),
    var searchResultTracks: List<TrackItem> = emptyList(),
    var searchResultPlaylist: List<PlaylistItem> = emptyList()
) : ScreenState


data class TrackItem(val _data: PlayListModel, override var isFavorite: Boolean = false) : Item {
    override val user = _data.user.username
    override val title = _data.title
    override val playlistTitle = _data.playlistTitle
    override val id = _data.id
    override val songIconList = _data.songImgList
    override val songCounter: String
        get() = TODO("Not yet implemented")
}