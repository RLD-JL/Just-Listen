package com.example.audius.viewmodel.screens.playlist

import com.example.audius.ScreenState
import com.example.audius.datalayer.models.PlayListModel
import com.example.audius.viewmodel.interfaces.Item

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
) : Item{
    override val user = _data.user.username
    override val title = _data.title
    override val playlistTitle = _data.playlistTitle
    override val id = _data.id
    override val songIconList = _data.songImgList
}

