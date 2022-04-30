package com.rld.justlisten.viewmodel.screens.addplaylist

import com.rld.justlisten.ScreenState
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.AddPlaylist

data class AddPlaylistState(
    val isLoading: Boolean = false,
    val playlistsCreated: List<AddPlaylist> = emptyList()
): ScreenState