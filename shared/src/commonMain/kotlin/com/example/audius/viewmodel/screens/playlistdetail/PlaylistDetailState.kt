package com.example.audius.viewmodel.screens.playlistdetail

import com.example.audius.ScreenState
import com.example.audius.datalayer.models.PlayListModel
import com.example.audius.viewmodel.screens.playlist.PlaylistItem

data class PlaylistDetailState(
    val isLoading: Boolean = false,
    val playlistName: String = "",
    val playListCreatedBy: String ="",
    var playlistIcon: String= "",
    var songPlaylist: List<PlaylistItem> = emptyList(),
    val dominantColor: Int = 0
): ScreenState
