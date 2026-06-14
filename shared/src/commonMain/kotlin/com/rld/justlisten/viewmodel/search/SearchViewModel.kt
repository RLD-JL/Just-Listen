package com.rld.justlisten.viewmodel.search

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.repositories.SearchRepository
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.search.SearchScreenState
import com.rld.justlisten.viewmodel.screens.search.SearchSeeAllType
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val favoritesRepository: FavoritesRepository,
) : BaseScreenViewModel() {
    
    private val _searchState = MutableStateFlow(SearchScreenState())
    val searchState: StateFlow<SearchScreenState> = _searchState.asStateFlow()

    private var autocompleteJob: Job? = null

    init {
        loadSearchHistory()
        viewModelScope.launch {
            favoritesRepository.getFavoritePlaylistFlow().collect { favoriteList ->
                val favoriteIds = favoriteList.map { it.id }.toSet()
                _searchState.update { state ->
                    val updatedTracks = state.searchResultTracks.map { item ->
                        item.copy(isFavorite = favoriteIds.contains(item.id))
                    }
                    state.copy(searchResultTracks = updatedTracks)
                }
            }
        }
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            val history = searchRepository.getSearchList()
            _searchState.update { it.copy(listOfSearches = history) }
        }
    }
    
    fun onSearchQueryChanged(query: String) {
        _searchState.update { it.copy(searchFor = query) }
        
        autocompleteJob?.cancel()
        if (query.isBlank()) {
            _searchState.update {
                it.copy(
                    searchResultTracks = emptyList(),
                    searchResultPlaylist = emptyList(),
                    searchResultUsers = emptyList(),
                    autocompleteTracks = emptyList(),
                    autocompletePlaylists = emptyList(),
                    autocompleteUsers = emptyList(),
                    isAutocompleteLoading = false,
                    seeAllType = SearchSeeAllType.NONE,
                    seeAllTracks = emptyList(),
                    seeAllPlaylists = emptyList(),
                    seeAllUsers = emptyList(),
                    seeAllOffset = 0,
                    seeAllLastItemReached = false,
                    isSeeAllLoading = false
                )
            }
            return
        }
        
        autocompleteJob = viewModelScope.launch {
            delay(300)
            _searchState.update { it.copy(isAutocompleteLoading = true) }
            try {
                val suggestions = searchRepository.searchAutocomplete(query)
                _searchState.update {
                    it.copy(
                        autocompleteTracks = suggestions.tracks,
                        autocompletePlaylists = suggestions.playlists,
                        autocompleteUsers = suggestions.users,
                        isAutocompleteLoading = false
                    )
                }
            } catch (e: Exception) {
                _searchState.update { it.copy(isAutocompleteLoading = false) }
            }
        }
    }
    
    fun onSearchSubmitted(query: String) {
        if (query.isBlank()) return
        autocompleteJob?.cancel()
        viewModelScope.launch {
            _searchState.update {
                it.copy(
                    isLoading = true,
                    searchFor = query,
                    autocompleteTracks = emptyList(),
                    autocompletePlaylists = emptyList(),
                    autocompleteUsers = emptyList(),
                    seeAllType = SearchSeeAllType.NONE,
                    seeAllTracks = emptyList(),
                    seeAllPlaylists = emptyList(),
                    seeAllUsers = emptyList(),
                    seeAllOffset = 0,
                    seeAllLastItemReached = false,
                    isSeeAllLoading = false
                )
            }
            try {
                searchRepository.saveSearch(query)
                val results = searchRepository.searchAutocomplete(query)
                _searchState.update {
                    it.copy(
                        isLoading = false,
                        searchResultTracks = results.tracks,
                        searchResultPlaylist = results.playlists,
                        searchResultUsers = results.users,
                        listOfSearches = searchRepository.getSearchList()
                    )
                }
            } catch (e: Exception) {
                _searchState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onSeeAllClicked(type: SearchSeeAllType) {
        if (type == SearchSeeAllType.NONE) {
            _searchState.update {
                it.copy(
                    seeAllType = SearchSeeAllType.NONE,
                    seeAllTracks = emptyList(),
                    seeAllPlaylists = emptyList(),
                    seeAllUsers = emptyList(),
                    seeAllOffset = 0,
                    seeAllLastItemReached = false,
                    isSeeAllLoading = false
                )
            }
            return
        }

        _searchState.update {
            it.copy(
                seeAllType = type,
                seeAllTracks = emptyList(),
                seeAllPlaylists = emptyList(),
                seeAllUsers = emptyList(),
                isSeeAllLoading = true,
                seeAllOffset = 0,
                seeAllLastItemReached = false
            )
        }
        
        loadMoreSeeAllItems()
    }

    fun loadMoreSeeAllItems() {
        val currentState = _searchState.value
        if (currentState.seeAllLastItemReached || currentState.seeAllType == SearchSeeAllType.NONE) return
        
        viewModelScope.launch {
            _searchState.update { it.copy(isSeeAllLoading = true) }
            val query = currentState.searchFor
            val offset = currentState.seeAllOffset
            val limit = 20
            
            try {
                when (currentState.seeAllType) {
                    SearchSeeAllType.SONGS -> {
                        val newTracks = searchRepository.searchForTracksPaginated(query, offset, limit)
                        _searchState.update { state ->
                            state.copy(
                                seeAllTracks = state.seeAllTracks + newTracks,
                                seeAllOffset = offset + limit,
                                isSeeAllLoading = false,
                                seeAllLastItemReached = newTracks.size < limit
                            )
                        }
                    }
                    SearchSeeAllType.PLAYLISTS -> {
                        val newPlaylists = searchRepository.searchForPlaylistsPaginated(query, offset, limit)
                        _searchState.update { state ->
                            state.copy(
                                seeAllPlaylists = state.seeAllPlaylists + newPlaylists,
                                seeAllOffset = offset + limit,
                                isSeeAllLoading = false,
                                seeAllLastItemReached = newPlaylists.size < limit
                            )
                        }
                    }
                    SearchSeeAllType.ARTISTS -> {
                        val newUsers = searchRepository.searchForUsersPaginated(query, offset, limit)
                        _searchState.update { state ->
                            state.copy(
                                seeAllUsers = state.seeAllUsers + newUsers,
                                seeAllOffset = offset + limit,
                                isSeeAllLoading = false,
                                seeAllLastItemReached = newUsers.size < limit
                            )
                        }
                    }
                    SearchSeeAllType.NONE -> {
                        _searchState.update { it.copy(isSeeAllLoading = false) }
                    }
                }
            } catch (e: Exception) {
                _searchState.update { it.copy(isSeeAllLoading = false) }
            }
        }
    }

    fun onPlaylistPressed(id: String, icon: String, title: String, createdBy: String) {
        navigate(
            Route.PlaylistDetail(
                playlistId = id,
                playlistIcon = icon,
                playlistTitle = title,
                playlistCreatedBy = createdBy,
                playlistEnum = "CURRENT_PLAYLIST",
            )
        )
    }

    fun popBack() {
        popBackStack()
    }

    fun onArtistClicked(artistId: String, artistName: String) {
        if (artistId.isNotBlank()) {
            navigate(Route.ArtistProfile(artistId, artistName))
        }
    }
}
