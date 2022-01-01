package com.example.audius.viewmodel.screens.library

import com.example.audius.ScreenState
import com.example.audius.viewmodel.screens.playlist.PlaylistItem

data class LibraryState(
    val isLoading: Boolean = false,
    val favoritePlaylistItems: List<PlaylistItem> = emptyList(),
    val recentSongsItems: List<PlaylistItem> = emptyList()
): ScreenState