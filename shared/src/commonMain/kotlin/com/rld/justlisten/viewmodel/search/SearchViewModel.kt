package com.rld.justlisten.viewmodel.search

import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.search.SearchScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchViewModel(
    private val repository: Repository,
) : BaseScreenViewModel() {
    
    private val _searchState = MutableStateFlow(SearchScreenState())
    val searchState: StateFlow<SearchScreenState> = _searchState.asStateFlow()
    
    fun onSearchQueryChanged(query: String) {
        _searchState.value = _searchState.value.copy(searchFor = query)
    }
    
    fun onSearchSubmitted(query: String) {
        // TODO: Implement search logic
    }

    fun onPlaylistPressed(id: String, icon: String, title: String, createdBy: String) {
        // TODO: Implement playlist navigation
    }

    fun popBack() {
        popBackStack()
    }
}
