package com.rld.justlisten.viewmodel.screens.playlistdetail

import com.rld.justlisten.ScreenState
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

data class PlaylistDetailState(
    val isLoading: Boolean = false,
    val playlistName: String = "",
    val playListCreatedBy: String ="",
    var playlistIcon: String= "",
    var songPlaylist: List<PlaylistItem> = emptyList(),
    val dominantColor: Int = 0
): ScreenState
