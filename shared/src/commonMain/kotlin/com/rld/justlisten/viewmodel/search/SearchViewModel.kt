package com.rld.justlisten.viewmodel.search

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.datacalls.search.saveSearch
import com.rld.justlisten.datalayer.datacalls.search.searchForPlaylist
import com.rld.justlisten.datalayer.datacalls.search.searchForTracks
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.search.SearchScreenState
import com.rld.justlisten.viewmodel.update
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
        loadPreviousSearches()
    }

    private fun loadPreviousSearches() {
        viewModelScope.launch {
            // Previous searches are loaded when user opens search; keep empty until first search
        }
    }

    fun onSearchSubmitted(query: String) {
        viewModelScope.launch {
            _searchState.update { it.copy(isLoading = true) }
            repository.saveSearch(query)
            coroutineScope {
                val tracks = async { repository.searchForTracks(query) }
                val playlists = async { repository.searchForPlaylist(query) }
                _searchState.update {
                    it.copy(
                        searchResultTracks = tracks.await(),
                        isLoading = false,
                        searchFor = query,
                        searchResultPlaylist = playlists.await(),
                    )
                }
            }
        }
    }

    fun onPlaylistPressed(
        playlistId: String,
        playlistIcon: String,
        playlistTitle: String,
        playlistCreatedBy: String,
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
