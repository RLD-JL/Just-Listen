package com.rld.justlisten.ui.actions

import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.TracksCategory
import com.rld.justlisten.viewmodel.screens.playlist.TimeRange

/**
 * Actions emitted by the Playlist screen and its child components.
 */
sealed interface PlaylistScreenAction {
    data class PlaylistClicked(
        val playlistId: String,
        val playlistIcon: String,
        val createdBy: String,
        val title: String,
    ) : PlaylistScreenAction

    data class SongPressed(
        val songId: String,
        val title: String,
        val user: String,
        val songIcon: SongIconList,
    ) : PlaylistScreenAction
    
    data class ArtistClicked(
        val artistId: String,
        val artistName: String
    ) : PlaylistScreenAction

    data object SearchClicked : PlaylistScreenAction
    data object RefreshScreen : PlaylistScreenAction
    data object NotificationsClicked : PlaylistScreenAction

    data class FetchMorePlaylists(
        val index: Int,
        val category: PlayListEnum,
        val query: String,
    ) : PlaylistScreenAction

    data class ChangeTracksCategory(
        val category: TracksCategory,
        val timeRange: TimeRange,
    ) : PlaylistScreenAction

    data class SeeAllClicked(
        val categoryName: String,
        val playlistEnum: PlayListEnum,
        val queryPlaylist: String = ""
    ) : PlaylistScreenAction

    data class SeeAllTracksClicked(
        val categoryName: String,
        val queryPlaylist: String,
        val selectedTimeRange: TimeRange
    ) : PlaylistScreenAction
}
