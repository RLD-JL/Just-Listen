package com.rld.justlisten.viewmodel.search

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.repositories.SearchRepository
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.search.SearchScreenState
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val favoritesRepository: FavoritesRepository,
) : BaseScreenViewModel() {
    
    private val _searchState = MutableStateFlow(SearchScreenState())
    val searchState: StateFlow<SearchScreenState> = _searchState.asStateFlow()

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
        _searchState.value = _searchState.value.copy(searchFor = query)
    }
    
    fun onSearchSubmitted(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _searchState.update { it.copy(isLoading = true, searchFor = query) }
            try {
                searchRepository.saveSearch(query)
                val tracks = searchRepository.searchForTracks(query)
                val playlists = searchRepository.searchForPlaylist(query)
                _searchState.update {
                    it.copy(
                        isLoading = false,
                        searchResultTracks = tracks,
                        searchResultPlaylist = playlists,
                        listOfSearches = searchRepository.getSearchList()
                    )
                }
            } catch (e: Exception) {
                _searchState.update { it.copy(isLoading = false) }
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
}
