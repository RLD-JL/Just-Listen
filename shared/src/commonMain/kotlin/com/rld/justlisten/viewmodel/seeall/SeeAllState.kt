package com.rld.justlisten.viewmodel.seeall

import com.rld.justlisten.ScreenState
import com.rld.justlisten.viewmodel.interfaces.Item
import com.rld.justlisten.viewmodel.screens.playlist.TimeRange

data class SeeAllState(
    val isLoading: Boolean = false,
    val isHeaderLoading: Boolean = false,
    val title: String = "",
    val items: List<Item> = emptyList(),
    val selectedTimeRange: TimeRange = TimeRange.ALLTIME,
    val playlistEnum: String = "",
    val queryPlaylist: String = "",
    val lastItemReached: Boolean = false
) : ScreenState
