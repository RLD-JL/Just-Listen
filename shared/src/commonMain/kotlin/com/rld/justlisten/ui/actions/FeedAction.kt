package com.rld.justlisten.ui.actions

import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel

import com.rld.justlisten.viewmodel.feed.FeedTab
import com.rld.justlisten.viewmodel.feed.FeedFilter
import com.rld.justlisten.viewmodel.feed.FeedFormat
import com.rld.justlisten.viewmodel.screens.playlist.TracksCategory
import com.rld.justlisten.viewmodel.screens.playlist.TimeRange

sealed interface FeedAction {
    data class SongPressed(val songId: String) : FeedAction
    data class PlaylistClicked(
        val playlistId: String,
        val playlistIcon: String,
        val createdBy: String,
        val title: String
    ) : FeedAction
    data class FavoritePressed(
        val songId: String,
        val title: String,
        val user: UserModel,
        val songIcon: SongIconList,
        val isFavorite: Boolean
    ) : FeedAction
    data class RepostPressed(
        val itemId: String,
        val isRepost: Boolean,
        val isPlaylist: Boolean = false
    ) : FeedAction
    data class ArtistClicked(val artistId: String, val artistName: String) : FeedAction
    data object Refresh : FeedAction
    data object DismissConnectPrompt : FeedAction
    data object ConnectAudiusPressed : FeedAction
    data object LoadMore : FeedAction
    data class SelectTab(val tab: FeedTab) : FeedAction
    data class SetPersonalFilter(val filter: FeedFilter) : FeedAction
    data class SetPersonalFormat(val format: FeedFormat) : FeedAction
    data class SetTrendingCategory(val category: TracksCategory) : FeedAction
    data class SetTrendingTimeRange(val timeRange: TimeRange) : FeedAction
}
