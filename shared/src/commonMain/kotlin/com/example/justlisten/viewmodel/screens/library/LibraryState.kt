package com.example.justlisten.viewmodel.screens.library

import com.example.justlisten.ScreenState
import com.example.justlisten.viewmodel.screens.playlist.PlaylistItem

data class LibraryState(
    val isLoading: Boolean = false,
    val favoritePlaylistItems: List<PlaylistItem> = emptyList(),
    val recentSongsItems: List<PlaylistItem> = emptyList()
): ScreenState