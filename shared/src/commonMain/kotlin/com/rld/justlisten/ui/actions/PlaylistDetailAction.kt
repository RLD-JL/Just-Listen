package com.rld.justlisten.ui.actions

import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel

/**
 * Actions emitted by the Playlist Detail screen and its child components.
 */
sealed interface PlaylistDetailAction {
    data class BackPressed(val isFromBottomSheet: Boolean) : PlaylistDetailAction

    data class SongPressed(val songId: String) : PlaylistDetailAction

    data class FavoritePressed(
        val songId: String,
        val title: String,
        val user: UserModel,
        val songIcon: SongIconList,
        val isFavorite: Boolean
    ) : PlaylistDetailAction

    data class DeletePlaylistClicked(val playlistName: String) : PlaylistDetailAction
    data class EditPlaylistTitleClicked(val oldName: String, val newName: String) : PlaylistDetailAction
}

