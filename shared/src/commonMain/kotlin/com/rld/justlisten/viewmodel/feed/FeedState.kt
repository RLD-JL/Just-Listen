package com.rld.justlisten.viewmodel.feed

import com.rld.justlisten.ScreenState
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.screens.playlist.TimeRange
import com.rld.justlisten.viewmodel.screens.playlist.TracksCategory
import androidx.compose.runtime.Immutable

enum class FeedTab {
    FOLLOWING,
    TRENDING
}

enum class FeedFilter(val value: String) {
    ALL("all"),
    ORIGINAL("original"),
    REPOST("repost")
}

enum class FeedFormat {
    ALL,
    TRACKS,
    PLAYLISTS,
    ALBUMS
}


@Immutable
data class FeedState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val items: List<PlaylistItem> = emptyList(),
    val rawItems: List<PlaylistItem> = emptyList(),
    val isGuest: Boolean = true,
    val showConnectPrompt: Boolean = false,
    val lastItemReached: Boolean = false,
    val selectedTab: FeedTab = FeedTab.FOLLOWING,
    val personalFilter: FeedFilter = FeedFilter.ALL,
    val personalFormat: FeedFormat = FeedFormat.ALL,
    val trendingCategory: TracksCategory = TracksCategory.ALL,
    val trendingTimeRange: TimeRange = TimeRange.WEEK
) : ScreenState
