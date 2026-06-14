package com.rld.justlisten.ui.actions

/**
 * Actions emitted by the Add Playlist screen and its child components.
 */
sealed interface AddPlaylistAction {
    data class BackPressed(val isFromBottomSheet: Boolean) : AddPlaylistAction

    data class AddPlaylistClicked(
        val playlistName: String,
        val playlistDescription: String?
    ) : AddPlaylistAction

    data class AddSongToPlaylist(
        val playlistTitle: String,
        val playlistDescription: String?,
        val songs: List<String>
    ) : AddPlaylistAction
}
