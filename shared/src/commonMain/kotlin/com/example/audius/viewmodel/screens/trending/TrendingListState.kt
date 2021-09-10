package com.example.audius.viewmodel.screens.trending

import com.example.audius.ScreenState
import com.example.audius.datalayer.models.PlayListModel
import com.example.audius.datalayer.models.TrendingListModel

data class TrendingListState(
    val isLoading: Boolean = false,
    val playMusic: Boolean = true,
    var skipToNext: Boolean = false,
    var songId: String = "",
    var songIcon: String= "",
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


data class PlaylistState(
    val isLoading: Boolean = false,
    val playlistName: String = "",
    val playListCreatedBy: String ="",
    var playlistIcon: String= "",
    val playlistItems: List<PlaylistItem> = emptyList(),
    val remixPlaylist: List<PlaylistItem> = emptyList(),
    val currentPlaylist: List<PlaylistItem> = emptyList()
): ScreenState

data class PlaylistItem(
    val _data: PlayListModel,
) {
    val user = _data.user.username
    val title = _data.title
    val playlistTitle = _data.playlistTitle
    val id = _data.id
    val songIconList = _data.songImgList
}