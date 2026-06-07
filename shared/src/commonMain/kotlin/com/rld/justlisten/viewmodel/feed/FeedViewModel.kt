package com.rld.justlisten.viewmodel.feed

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.repositories.FeedRepository
import com.rld.justlisten.datalayer.repositories.PlaylistRepository
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import com.rld.justlisten.datalayer.repositories.AuthRepository
import com.rld.justlisten.datalayer.repositories.SessionState
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.screens.playlist.TimeRange
import com.rld.justlisten.viewmodel.screens.playlist.TracksCategory

class FeedViewModel(
    private val feedRepository: FeedRepository,
    private val playlistRepository: PlaylistRepository,
    private val favoritesRepository: FavoritesRepository,
    private val authRepository: AuthRepository
) : BaseScreenViewModel() {

    private val _feedState = MutableStateFlow(FeedState())
    val feedState: StateFlow<FeedState> = _feedState.asStateFlow()

    init {
        // Observe auth state to fetch or clear the feed
        viewModelScope.launch {
            authRepository.sessionState.collect { session ->
                when (session) {
                    is SessionState.Authenticated -> {
                        _feedState.update { it.copy(isGuest = false) }
                        refreshActiveTab()
                    }
                    is SessionState.Guest -> {
                        _feedState.update { it.copy(isGuest = true) }
                        refreshActiveTab()
                    }
                }
            }
        }

        // Observe favorites to keep items in sync
        viewModelScope.launch {
            favoritesRepository.getFavoritePlaylistFlow().collect { favoriteList ->
                val favoriteIds = favoriteList.map { it.id }.toSet()
                _feedState.update { state ->
                    val updatedRaw = state.rawItems.map { item ->
                        item.copy(isFavorite = favoriteIds.contains(item.id))
                    }
                    val updatedItems = state.items.map { item ->
                        item.copy(isFavorite = favoriteIds.contains(item.id))
                    }
                    state.copy(rawItems = updatedRaw, items = updatedItems)
                }
            }
        }

        // Observe reposts to keep items in sync
        viewModelScope.launch {
            playlistRepository.repostedTrackIdsFlow.collect { repostedIds ->
                _feedState.update { state ->
                    val updatedRaw = state.rawItems.map { item ->
                        item.copy(isReposted = repostedIds.contains(item.id))
                    }
                    val updatedItems = state.items.map { item ->
                        item.copy(isReposted = repostedIds.contains(item.id))
                    }
                    state.copy(rawItems = updatedRaw, items = updatedItems)
                }
            }
        }
    }

    private fun filterFeedItems(items: List<PlaylistItem>, format: FeedFormat): List<PlaylistItem> {
        return when (format) {
            FeedFormat.ALL -> items
            FeedFormat.TRACKS -> items.filter { !it._data.isPlaylist }
            FeedFormat.PLAYLISTS -> items.filter { it._data.isPlaylist && !it._data.isAlbum }
            FeedFormat.ALBUMS -> items.filter { it._data.isPlaylist && it._data.isAlbum }
        }
    }

    fun fetchFeed(userId: String) {
        viewModelScope.launch {
            _feedState.update { it.copy(isLoading = true, lastItemReached = false, items = emptyList(), rawItems = emptyList()) }
            try {
                val currentState = _feedState.value
                val tracksOnly = if (currentState.personalFormat == FeedFormat.TRACKS) true else null
                val feedItems = feedRepository.getUserFeed(
                    userId = userId,
                    limit = 20,
                    offset = 0,
                    filter = currentState.personalFilter.value,
                    tracksOnly = tracksOnly
                )
                val filteredItems = filterFeedItems(feedItems, currentState.personalFormat)
                _feedState.update { 
                    it.copy(
                        rawItems = feedItems,
                        items = filteredItems,
                        isLoading = false,
                        lastItemReached = feedItems.size < 20
                    ) 
                }
            } catch (e: Exception) {
                _feedState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun fetchTrending(category: TracksCategory, timeRange: TimeRange) {
        viewModelScope.launch {
            _feedState.update { it.copy(isLoading = true, lastItemReached = false, items = emptyList(), rawItems = emptyList()) }
            try {
                val genre = if (category == TracksCategory.ALL) "" else category.value
                val time = when (timeRange) {
                    TimeRange.ALLTIME -> "allTime"
                    TimeRange.MONTH -> "month"
                    TimeRange.WEEK -> "week"
                }
                val tracks = playlistRepository.getTracks(20, genre, time)
                val playlistItems = tracks.map { track ->
                    PlaylistItem(_data = track._data, isFavorite = track.isFavorite, isReposted = track.isReposted)
                }
                _feedState.update { 
                    it.copy(
                        rawItems = playlistItems,
                        items = playlistItems,
                        isLoading = false,
                        lastItemReached = true
                    ) 
                }
            } catch (e: Exception) {
                _feedState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun selectTab(tab: FeedTab) {
        _feedState.update { it.copy(selectedTab = tab) }
        refreshActiveTab()
    }

    fun setPersonalFilter(filter: FeedFilter) {
        _feedState.update { it.copy(personalFilter = filter) }
        val session = authRepository.sessionState.value
        if (session is SessionState.Authenticated) {
            fetchFeed(session.userProfile.userId ?: "")
        }
    }

    fun setPersonalFormat(format: FeedFormat) {
        _feedState.update { it.copy(personalFormat = format) }
        val session = authRepository.sessionState.value
        if (session is SessionState.Authenticated) {
            fetchFeed(session.userProfile.userId ?: "")
        }
    }

    fun setTrendingCategory(category: TracksCategory) {
        _feedState.update { it.copy(trendingCategory = category) }
        fetchTrending(category, _feedState.value.trendingTimeRange)
    }

    fun setTrendingTimeRange(timeRange: TimeRange) {
        _feedState.update { it.copy(trendingTimeRange = timeRange) }
        fetchTrending(_feedState.value.trendingCategory, timeRange)
    }

    fun refreshActiveTab() {
        val currentState = _feedState.value
        if (currentState.selectedTab == FeedTab.FOLLOWING) {
            val session = authRepository.sessionState.value
            if (session is SessionState.Authenticated) {
                fetchFeed(session.userProfile.userId ?: "")
            } else {
                _feedState.update { it.copy(isGuest = true, items = emptyList(), rawItems = emptyList(), isLoading = false) }
            }
        } else {
            fetchTrending(currentState.trendingCategory, currentState.trendingTimeRange)
        }
    }

    fun loadMore() {
        val currentState = _feedState.value
        if (currentState.isLoading || currentState.lastItemReached) {
            return
        }
        if (currentState.selectedTab == FeedTab.TRENDING) {
            return
        }
        val session = authRepository.sessionState.value
        if (session !is SessionState.Authenticated) {
            return
        }
        val userId = session.userProfile.userId ?: ""
        viewModelScope.launch {
            _feedState.update { it.copy(isLoading = true) }
            try {
                val currentOffset = currentState.rawItems.size
                val limit = 20
                val tracksOnly = if (currentState.personalFormat == FeedFormat.TRACKS) true else null
                val newItems = feedRepository.getUserFeed(
                    userId = userId,
                    limit = limit,
                    offset = currentOffset,
                    filter = currentState.personalFilter.value,
                    tracksOnly = tracksOnly
                )
                _feedState.update { state ->
                    val updatedRaw = state.rawItems + newItems
                    val filteredItems = filterFeedItems(updatedRaw, state.personalFormat)
                    state.copy(
                        rawItems = updatedRaw,
                        items = filteredItems,
                        isLoading = false,
                        lastItemReached = newItems.size < limit
                    )
                }
            } catch (e: Exception) {
                _feedState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onFavoritePressed(
        songId: String,
        title: String,
        user: com.rld.justlisten.datalayer.models.UserModel,
        songIcon: com.rld.justlisten.datalayer.models.SongIconList,
        isFavorite: Boolean
    ) {
        viewModelScope.launch {
            favoritesRepository.saveSongToFavorites(songId, title, user, songIcon, "Favorite", isFavorite)
        }
    }

    fun onRepostPressed(itemId: String, isRepost: Boolean, isPlaylist: Boolean) {
        if (authRepository.sessionState.value is SessionState.Guest) {
            _feedState.update { it.copy(showConnectPrompt = true) }
            return
        }

        viewModelScope.launch {
            val success = if (isPlaylist) {
                if (isRepost) {
                    playlistRepository.repostPlaylist(itemId)
                } else {
                    playlistRepository.unrepostPlaylist(itemId)
                }
            } else {
                if (isRepost) {
                    playlistRepository.repostTrack(itemId)
                } else {
                    playlistRepository.unrepostTrack(itemId)
                }
            }

            if (success) {
                // Instantly update local UI counts
                _feedState.update { state ->
                    val updateItem = { item: PlaylistItem ->
                        if (item.id == itemId) {
                            val newCount = if (isRepost) item.repostCount + 1 else (item.repostCount - 1).coerceAtLeast(0)
                            val updatedData = item._data.copy(repostCount = newCount)
                            item.copy(_data = updatedData, isReposted = isRepost)
                        } else item
                    }
                    val updatedRaw = state.rawItems.map(updateItem)
                    val updatedItems = state.items.map(updateItem)
                    state.copy(rawItems = updatedRaw, items = updatedItems)
                }
            }
        }
    }

    fun dismissConnectPrompt() {
        _feedState.update { it.copy(showConnectPrompt = false) }
    }

    fun refreshFeed() {
        refreshActiveTab()
    }

    fun onPlaylistClicked(playlistId: String, playlistIcon: String, createdBy: String, title: String) {
        navigate(
            Route.PlaylistDetail(
                playlistId = playlistId,
                playlistIcon = playlistIcon,
                playlistTitle = title,
                playlistCreatedBy = createdBy,
                playlistEnum = "CURRENT_PLAYLIST"
            )
        )
    }

    fun onArtistClicked(artistId: String, artistName: String) {
        if (artistId.isNotBlank()) {
            navigate(Route.ArtistProfile(artistId, artistName))
        }
    }
}
