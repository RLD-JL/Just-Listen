package com.rld.justlisten.viewmodel.screens.playlist

import com.rld.justlisten.ScreenState
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.viewmodel.interfaces.Item

data class PlaylistState(
    val isLoading: Boolean = false,
    val playlistName: String = "",
    val playListCreatedBy: String ="",
    var playlistIcon: String= "",
    val playlistItems: List<PlaylistItem> = emptyList(),
    val remixPlaylist: List<PlaylistItem> = emptyList(),
    val hotPlaylist: List<PlaylistItem> = emptyList(),
    val currentPlaylist: List<PlaylistItem> = emptyList(),
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
}

