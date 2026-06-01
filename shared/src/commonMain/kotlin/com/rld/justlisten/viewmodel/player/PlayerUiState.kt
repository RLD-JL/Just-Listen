package com.rld.justlisten.viewmodel.player

import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.media.PlaybackState

data class PlayerUiState(
    val addPlaylistList: List<AddPlaylist> = emptyList(),
    val playbackState: PlaybackState? = null
)
