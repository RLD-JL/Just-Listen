package com.rld.justlisten.viewmodel.search

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.datacalls.search.getSearchList
import com.rld.justlisten.datalayer.datacalls.search.saveSearch
import com.rld.justlisten.datalayer.datacalls.search.searchForPlaylist
import com.rld.justlisten.datalayer.datacalls.search.searchForTracks
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.search.SearchScreenState
import com.rld.justlisten.viewmodel.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val repository: Repository,
) : BaseScreenViewModel() {
    
    private val _searchState = MutableStateFlow(SearchScreenState())
    val searchState: StateFlow<SearchScreenState> = _searchState.asStateFlow()

    init {
        loadSearchHistory()
        viewModelScope.launch {
            repository.favoriteEvents.collect { (songId, isFavorite) ->
                _searchState.update { state ->
                    state.copy(
                        searchResultTracks = state.searchResultTracks.map { item ->
                            if (item.id == songId) {
                                item.copy(isFavorite = isFavorite)
                            } else {
                                item
                            }
                        }
                    )
                }
            }
        }
    }

    private fun loadSearchHistory() {
        viewModelScope.launch {
            val history = repository.getSearchList()
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
                repository.saveSearch(query)
                val tracks = repository.searchForTracks(query)
                val playlists = repository.searchForPlaylist(query)
                _searchState.update {
                    it.copy(
                        isLoading = false,
                        searchResultTracks = tracks,
                        searchResultPlaylist = playlists,
                        listOfSearches = repository.getSearchList()
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
