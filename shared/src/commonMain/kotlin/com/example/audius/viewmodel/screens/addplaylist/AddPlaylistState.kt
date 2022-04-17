package com.example.audius.viewmodel.screens.addplaylist

import com.example.audius.ScreenState
import com.example.audius.datalayer.localdb.addplaylistscreen.AddPlaylist

data class AddPlaylistState(
    val isLoading: Boolean = false,
    val playlistsCreated: List<AddPlaylist> = emptyList()
): ScreenState