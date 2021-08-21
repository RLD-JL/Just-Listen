package com.example.audius.viewmodel.screens.trending

import com.example.audius.ScreenState
import com.example.audius.datalayer.models.TrendingListModel

data class TrendingListState(
    val isLoading: Boolean = false,
    val playMusic: Boolean = true,
    val songId: String = "",
    val songIcon: String= "",
    val trendingListItems: List<TrendingListItem> = emptyList()
): ScreenState


data class TrendingListItem(
    val _data: TrendingListModel,
) {
    val title = _data.title
    val id = _data.id
    val favouriteCount = _data.favouriteCount
    val repostCount = _data.repostCount
    val songIconList = _data.songImgList
}