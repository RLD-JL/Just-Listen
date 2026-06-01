package com.rld.justlisten.viewmodel.seeall

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.repositories.PlaylistRepository
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.screens.search.TrackItem
import com.rld.justlisten.viewmodel.screens.playlist.TimeRange
import com.rld.justlisten.viewmodel.screens.playlist.TracksCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SeeAllViewModel(
    private val playlistRepository: PlaylistRepository,
    private val favoritesRepository: FavoritesRepository,
) : BaseScreenViewModel() {

    private val _seeAllState = MutableStateFlow(SeeAllState(isLoading = true))
    val seeAllState: StateFlow<SeeAllState> = _seeAllState.asStateFlow()

    init {
        viewModelScope.launch {
            favoritesRepository.getFavoritePlaylistFlow().collect { favoriteList ->
                val favoriteIds = favoriteList.map { it.id }.toSet()
                _seeAllState.update { state ->
                    val updated = state.items.map { item ->
                        when (item) {
                            is PlaylistItem -> item.copy(isFavorite = favoriteIds.contains(item.id))
                            is TrackItem -> item.copy(isFavorite = favoriteIds.contains(item.id))
                            else -> item
                        }
                    }
                    state.copy(items = updated)
                }
            }
        }
    }

    fun load(args: Route.SeeAll) {
        val timeRange = try { TimeRange.valueOf(args.selectedTimeRange) } catch(e: Exception) { TimeRange.WEEK }
        if (_seeAllState.value.title == args.categoryName && _seeAllState.value.items.isNotEmpty()) {
            return
        }
        _seeAllState.update {
            it.copy(
                isLoading = true,
                title = args.categoryName,
                playlistEnum = args.playlistEnum,
                queryPlaylist = args.queryPlaylist,
                selectedTimeRange = timeRange,
                items = emptyList(),
                lastItemReached = false
            )
        }
        fetchItems(offset = 20)
    }

    fun fetchItems(offset: Int) {
        _seeAllState.update { it.copy(isLoading = true) }
        val currentState = _seeAllState.value
        viewModelScope.launch {
            val limit = offset
            val items = if (currentState.playlistEnum == "TRACKS") {
                val time = when (currentState.selectedTimeRange) {
                    TimeRange.ALLTIME -> "allTime"
                    TimeRange.MONTH -> "month"
                    TimeRange.WEEK -> "week"
                }
                val searchCategory = if (currentState.queryPlaylist == "All") "" else if (currentState.queryPlaylist == "Rap") "Hip-Hop/Rap" else currentState.queryPlaylist
                playlistRepository.getTracks(limit, searchCategory, time)
            } else {
                val playListEnum = PlayListEnum.valueOf(currentState.playlistEnum)
                playlistRepository.getPlaylist(
                    index = limit,
                    playListEnum = playListEnum,
                    queryPlaylist = currentState.queryPlaylist
                )
            }
            
            val favoriteList = favoritesRepository.getFavoritePlaylist()
            val favoriteIds = favoriteList.map { it.id }.toSet()
            val mappedItems = items.map { item ->
                when (item) {
                    is PlaylistItem -> item.copy(isFavorite = favoriteIds.contains(item.id))
                    is TrackItem -> item.copy(isFavorite = favoriteIds.contains(item.id))
                    else -> item
                }
            }

            _seeAllState.update { state ->
                if (mappedItems.size == state.items.size && state.items.isNotEmpty()) {
                    state.copy(isLoading = false, isHeaderLoading = false, lastItemReached = true)
                } else {
                    state.copy(
                        isLoading = false,
                        isHeaderLoading = false,
                        items = mappedItems,
                        lastItemReached = mappedItems.size < limit
                    )
                }
            }
        }
    }

    fun changeTimeRange(timeRange: TimeRange) {
        _seeAllState.update {
            it.copy(
                isLoading = true,
                selectedTimeRange = timeRange,
                items = emptyList(),
                lastItemReached = false
            )
        }
        fetchItems(offset = 20)
    }

    fun changeGenre(category: TracksCategory) {
        _seeAllState.update {
            it.copy(
                isLoading = true,
                title = "${category.value} Trending",
                queryPlaylist = category.value,
                items = emptyList(),
                lastItemReached = false
            )
        }
        fetchItems(offset = 20)
    }

    fun onPlaylistClicked(
        playlistId: String,
        playlistIcon: String,
        playlistCreatedBy: String,
        playlistTitle: String,
    ) {
        navigate(
            Route.PlaylistDetail(
                playlistId = playlistId,
                playlistIcon = playlistIcon,
                playlistTitle = playlistTitle,
                playlistCreatedBy = playlistCreatedBy,
                playlistEnum = "CURRENT_PLAYLIST",
            ),
        )
    }

    fun popBack() {
        popBackStack()
    }
}
