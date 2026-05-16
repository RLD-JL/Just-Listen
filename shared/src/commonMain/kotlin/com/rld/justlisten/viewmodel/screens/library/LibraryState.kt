package com.rld.justlisten.viewmodel.screens.library

import com.rld.justlisten.ScreenState
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist

data class LibraryState(
    val isLoading: Boolean = false,
    val favoritePlaylistItems: List<PlaylistItem> = emptyList(),
    val mostPlayedSongs: List<PlaylistItem> = emptyList(),
    val recentSongsItems: List<PlaylistItem> = emptyList(),
    val playlistsCreated: List<AddPlaylist> = emptyList(),
    var lastIndexReached: Boolean = false
): ScreenState
