package com.rld.justlisten.ui.searchscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.rld.justlisten.ui.extensions.noRippleClickable
import com.rld.justlisten.ui.loadingscreen.LoadingScreen
import com.rld.justlisten.ui.searchscreen.components.AnimatedToolBar
import com.rld.justlisten.ui.searchscreen.components.ShowPreviousSearches
import com.rld.justlisten.ui.searchscreen.components.ShowSearchResults
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.viewmodel.screens.search.SearchScreenState

import com.rld.justlisten.ui.actions.SearchScreenAction

@ExperimentalComposeUiApi
@Composable
fun SearchScreen(
    searchScreenState: SearchScreenState,
    onAction: (SearchScreenAction) -> Unit
)
{
    val requester = FocusRequester()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState(0)
    var searchFor by rememberSaveable { mutableStateOf("") }

    Box(
        Modifier
            .fillMaxSize()
            .noRippleClickable(onClick = { focusManager.clearFocus() })
    ) {

        Column(
            Modifier
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(scrollState)
        ) {
            AnimatedToolBar(
               onBackPressed = { onAction(SearchScreenAction.BackPressed(it)) },
               requester = requester,
                onSearchPressed = { onAction(SearchScreenAction.SearchPressed(it)) },
                updateSearch = { updateSearch ->
                    searchFor = updateSearch.trimStart { it == '0' }
                },
               searchFor = searchFor
            )
            when {
                searchScreenState.isLoading -> {
                    LoadingScreen(10.dp)
                }
                searchScreenState.searchResultTracks.isEmpty() -> {
                    ShowPreviousSearches(searchScreenState.listOfSearches,
                        onPreviousSearchedPressed = { searched ->
                            searchFor = searched
                        })
                }
                else -> {
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
            }
        }
    }
}
