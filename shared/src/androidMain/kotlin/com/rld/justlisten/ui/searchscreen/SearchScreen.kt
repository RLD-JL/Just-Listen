package com.rld.justlisten.ui.searchscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.rld.justlisten.ui.extensions.noRippleClickable
import com.rld.justlisten.ui.loadingscreen.LoadingScreen
import com.rld.justlisten.ui.searchscreen.components.ShowPreviousSearches
import com.rld.justlisten.ui.searchscreen.components.ShowSearchResults
import com.rld.justlisten.ui.searchscreen.components.SearchCategoryDashboard
import com.rld.justlisten.ui.searchscreen.components.AutocompleteSuggestionsList
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.viewmodel.screens.search.SearchScreenState
import com.rld.justlisten.ui.actions.SearchScreenAction

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalComposeUiApi
@Composable
fun SearchScreen(
    searchScreenState: SearchScreenState,
    onAction: (SearchScreenAction) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState(0)
    var active by rememberSaveable { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .noRippleClickable(onClick = { focusManager.clearFocus() })
    ) {
        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            SearchBar(
                query = searchScreenState.searchFor,
                onQueryChange = { onAction(SearchScreenAction.QueryChanged(it)) },
                onSearch = {
                    active = false
                    onAction(SearchScreenAction.SearchPressed(it))
                },
                active = active,
                onActiveChange = { active = it },
                placeholder = { Text("Search songs, artists, playlists...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchScreenState.searchFor.isNotEmpty()) {
                        IconButton(onClick = { onAction(SearchScreenAction.QueryChanged("")) }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (active) 0.dp else 16.dp, vertical = 8.dp),
                content = {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (searchScreenState.searchFor.isBlank()) {
                            ShowPreviousSearches(
                                listOfSearches = searchScreenState.listOfSearches,
                                onPreviousSearchedPressed = { searched ->
                                    active = false
                                    onAction(SearchScreenAction.SearchPressed(searched))
                                }
                            )
                        } else {
                            if (searchScreenState.isAutocompleteLoading) {
                                LoadingScreen(10.dp)
                            } else {
                                AutocompleteSuggestionsList(
                                    tracks = searchScreenState.autocompleteTracks,
                                    playlists = searchScreenState.autocompletePlaylists,
                                    users = searchScreenState.autocompleteUsers,
                                    onSongPressed = { id, title, user, icon ->
                                        active = false
                                        onAction(SearchScreenAction.SongPressed(id, title, user, icon))
                                    },
                                    onPlaylistPressed = { id, icon, title, creator, fav ->
                                        active = false
                                        onAction(SearchScreenAction.PlaylistPressed(id, icon, title, creator, fav))
                                    },
                                    onUserPressed = { artistName ->
                                        active = false
                                        onAction(SearchScreenAction.SearchPressed(artistName))
                                    }
                                )
                            }
                        }
                    }
                }
            )

            if (!active) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    when {
                        searchScreenState.isLoading -> {
                            LoadingScreen(10.dp)
                        }
                        searchScreenState.searchResultTracks.isNotEmpty() -> {
                            ShowSearchResults(
                                searchScreenState.searchResultTracks,
                                searchScreenState.searchResultPlaylist,
                                onSongPressed = { songId, title, user, songIcon ->
                                    onAction(SearchScreenAction.SongPressed(songId, title, user, songIcon))
                                },
                                onPlaylistPressed = { id, icon, title, createdBy, isFavorite ->
                                    onAction(SearchScreenAction.PlaylistPressed(id, icon, title, createdBy, isFavorite))
                                }
                            )
                        }
                        else -> {
                            SearchCategoryDashboard(
                                onCategoryClick = { category ->
                                    onAction(SearchScreenAction.SearchPressed(category))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
