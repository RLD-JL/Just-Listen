package com.rld.justlisten.viewmodel.screens.playlist

import com.rld.justlisten.ScreenState
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.viewmodel.interfaces.Item
import com.rld.justlisten.viewmodel.screens.search.TrackItem
import com.rld.justlisten.datalayer.repositories.SessionState

import androidx.compose.runtime.Immutable

@Immutable
data class PlaylistState(
    val isLoading: Boolean = false,
    val playlistName: String = "",
    val playListCreatedBy: String ="",
    val playlistIcon: String= "",
    val playlistItems: List<PlaylistItem> = emptyList(),
    val lastFetchPlaylist: Boolean = false,
    val remixPlaylist: List<PlaylistItem> = emptyList(),
    val lastFetchRemix: Boolean = false,
    val hotPlaylist: List<PlaylistItem> = emptyList(),
    val lastFetchHot: Boolean = false,
    val currentPlaylist: List<PlaylistItem> = emptyList(),
    val tracksLoading: Boolean = false,
    val tracksList: List<TrackItem> = emptyList(),
    val queryIndex: Int = 0,
    val queryIndex2: Int = 0,
    val selectedCategory: TracksCategory = TracksCategory.ALL,
    val selectedTimeRange: TimeRange = TimeRange.WEEK,
    val sessionState: SessionState = SessionState.Guest
): ScreenState

data class PlaylistItem(
    val _data: PlayListModel,
    override val isFavorite: Boolean = false,
    override val isReposted: Boolean = _data.hasCurrentUserReposted,
    override val repostCount: Int = _data.repostCount,
    override val favoriteCount: Int = _data.favoriteCount
) : Item{
    override val user = _data.user.username
    override val title = _data.title
    override val playlistTitle = _data.playlistTitle
    override val id = _data.id
    override val songIconList = _data.songImgList
    override val songCounter = _data.songCounter
    override val durationPlayedSec = _data.durationPlayedSec
    override val commentCount = _data.commentCount
    override val playCount = if (_data.isPlaylist) _data.totalPlayCount else _data.playCount
    override val duration = _data.duration
    override val userId = _data.user.id
    val releaseDate = _data.releaseDate
}

