package com.rld.justlisten.ui.actions

/**
 * Actions emitted by the Library screen and its child components.
 */
sealed interface LibraryScreenAction {
    data class FavoritePlaylistPressed(
        val playlistId: String,
        val playlistIcon: String,
        val playlistTitle: String,
        val playlistCreatedBy: String
    ) : LibraryScreenAction

    data class MostPlayedPlaylistPressed(
        val playlistId: String,
        val playlistIcon: String,
        val playlistTitle: String,
        val playlistCreatedBy: String
    ) : LibraryScreenAction

    data object PlayListViewClicked : LibraryScreenAction

    data class PlaylistCreatedClicked(
        val title: String,
        val description: String?,
        val songs: List<String>
    ) : LibraryScreenAction

    data class DeletePlaylistClicked(val playlistName: String) : LibraryScreenAction

    data class LastItemReached(val index: Int) : LibraryScreenAction

    data object TimeCapsulePressed : LibraryScreenAction
    data object MusicInsightsPressed : LibraryScreenAction
    data object ExploreMusicPressed : LibraryScreenAction
}
