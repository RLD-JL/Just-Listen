package com.example.justlisten.viewmodel.screens.playlistdetail

import com.example.justlisten.ScreenState
import com.example.justlisten.datalayer.models.PlayListModel
import com.example.justlisten.viewmodel.screens.playlist.PlaylistItem

data class PlaylistDetailState(
    val isLoading: Boolean = false,
    val playlistName: String = "",
    val playListCreatedBy: String ="",
    var playlistIcon: String= "",
    var songPlaylist: List<PlaylistItem> = emptyList(),
    val dominantColor: Int = 0
): ScreenState
