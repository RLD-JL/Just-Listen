package com.rld.justlisten.ui.actions

import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel

/**
 * Actions emitted by the player bar UI.
 * Handled by PlayerViewModel to keep the composable layer free of business logic.
 */
sealed interface PlayerAction {
    data class ToggleFavorite(
        val songId: String,
        val title: String,
        val user: UserModel,
        val songIcon: SongIconList,
        val isFavorite: Boolean,
    ) : PlayerAction

    data object SkipNext : PlayerAction
    data object Collapse : PlayerAction
    data object ExpandMinibar : PlayerAction
    data class CreatePlaylist(val name: String, val description: String?) : PlayerAction
    data class AddSongToPlaylist(
        val playlistTitle: String,
        val playlistDescription: String?,
        val songs: List<String>,
    ) : PlayerAction
    data object LoadPlaylists : PlayerAction
    data class NewDominantColor(val colorInt: Int) : PlayerAction
}
