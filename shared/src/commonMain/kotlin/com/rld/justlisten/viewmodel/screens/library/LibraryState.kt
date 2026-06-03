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
    var lastIndexReached: Boolean = false,
    val timeCapsuleSongs: List<PlaylistItem> = emptyList(),
    val totalPlays: Int = 0,
    val uniquePlays: Int = 0,
    val hoursPlayed: Double = 0.0,
    val topArtistName: String = "",
    val topArtistPlays: Int = 0,
    val topArtistHours: Double = 0.0,
    val lastMostPlayedIndexReached: Boolean = false
): ScreenState
