package com.rld.justlisten.viewmodel.player

import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.media.PlaybackState
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

import androidx.compose.runtime.Immutable

@Immutable
data class PlayerUiState(
    val addPlaylistList: List<AddPlaylist> = emptyList(),
    val playbackState: PlaybackState? = null,
    val showConnectPrompt: Boolean = false,
    val isAutoplayEnabled: Boolean = true,
    val recommendedSongs: List<PlaylistItem> = emptyList()
)
