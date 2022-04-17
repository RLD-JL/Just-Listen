package com.example.audius.viewmodel.screens.addplaylist

import com.example.audius.ScreenState
import com.example.audius.viewmodel.screens.playlist.PlaylistItem

data class AddPlaylistState(
    val isLoading: Boolean = false,
    val playlistsCreated: List<PlaylistItem> = emptyList()
): ScreenState