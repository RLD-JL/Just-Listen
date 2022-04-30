package com.example.justlisten.viewmodel.screens.addplaylist

import com.example.justlisten.ScreenState
import com.example.justlisten.datalayer.localdb.addplaylistscreen.AddPlaylist

data class AddPlaylistState(
    val isLoading: Boolean = false,
    val playlistsCreated: List<AddPlaylist> = emptyList()
): ScreenState