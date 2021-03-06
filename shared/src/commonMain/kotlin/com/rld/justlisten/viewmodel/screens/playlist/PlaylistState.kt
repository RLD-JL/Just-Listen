package com.rld.justlisten.viewmodel.screens.playlist

import com.rld.justlisten.ScreenState
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.viewmodel.interfaces.Item
import com.rld.justlisten.viewmodel.screens.search.TrackItem

data class PlaylistState(
    val isLoading: Boolean = false,
    val playlistName: String = "",
    val playListCreatedBy: String ="",
    var playlistIcon: String= "",
    val playlistItems: List<PlaylistItem> = emptyList(),
    val lastFetchPlaylist: Boolean = false,
    val remixPlaylist: List<PlaylistItem> = emptyList(),
    val lastFetchRemix: Boolean = false,
    val hotPlaylist: List<PlaylistItem> = emptyList(),
    val lastFetchHot: Boolean = false,
    val currentPlaylist: List<PlaylistItem> = emptyList(),
    val tracksLoading: Boolean = false,
    var tracksList: List<TrackItem> = emptyList(),
    val queryIndex: Int = 0,
    val queryIndex2: Int = 0
): ScreenState

data class PlaylistItem(
    val _data: PlayListModel,
    override var isFavorite: Boolean = false
) : Item{
    override val user = _data.user.username
    override val title = _data.title
    override val playlistTitle = _data.playlistTitle
    override val id = _data.id
    override val songIconList = _data.songImgList
    override val songCounter = _data.songCounter
}

