package com.example.justlisten.viewmodel.screens.playlist

import com.example.justlisten.ScreenState
import com.example.justlisten.datalayer.models.PlayListModel
import com.example.justlisten.viewmodel.interfaces.Item

data class PlaylistState(
    val isLoading: Boolean = false,
    val playlistName: String = "",
    val playListCreatedBy: String ="",
    var playlistIcon: String= "",
    val playlistItems: List<PlaylistItem> = emptyList(),
    val remixPlaylist: List<PlaylistItem> = emptyList(),
    val currentPlaylist: List<PlaylistItem> = emptyList()
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

