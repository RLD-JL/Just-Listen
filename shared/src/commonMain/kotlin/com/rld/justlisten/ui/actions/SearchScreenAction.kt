package com.rld.justlisten.ui.actions

import com.rld.justlisten.datalayer.models.SongIconList

/**
 * Actions emitted by the Search screen and its child components.
 */
sealed interface SearchScreenAction {
    data class BackPressed(val isFromBottomSheet: Boolean) : SearchScreenAction

    data class SearchPressed(val query: String) : SearchScreenAction

    data class QueryChanged(val query: String) : SearchScreenAction

    data class SongPressed(
        val songId: String,
        val title: String,
        val user: String,
        val songIcon: SongIconList
    ) : SearchScreenAction

    data class PlaylistPressed(
        val playlistId: String,
        val playlistIcon: String,
        val playlistTitle: String,
        val playlistCreatedBy: String,
        val isFavorite: Boolean
    ) : SearchScreenAction
}
