package com.rld.justlisten.android.ui.searchscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
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
import com.rld.justlisten.android.ui.extensions.noRippleClickable
import com.rld.justlisten.android.ui.loadingscreen.LoadingScreen
import com.rld.justlisten.android.ui.searchscreen.components.AnimatedToolBar
import com.rld.justlisten.android.ui.searchscreen.components.ShowPreviousSearches
import com.rld.justlisten.android.ui.searchscreen.components.ShowSearchResults
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.viewmodel.screens.search.SearchScreenState

@ExperimentalComposeUiApi
@Composable
fun SearchScreen(
    onBackPressed: (Boolean) -> Unit,
    onSearchPressed: (String) -> Unit,
    onSongPressed: (String, String, String, SongIconList) -> Unit,
    onPlaylistPressed: (String, String, String, String, Boolean) -> Unit,
    searchScreenState: SearchScreenState)
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
                .background(MaterialTheme.colors.background)
                .verticalScroll(scrollState)
        ) {
            AnimatedToolBar(
               onBackPressed = onBackPressed,
               requester = requester,
                onSearchPressed = onSearchPressed,
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
                        searchScreenState.searchResultPlaylist, onSongPressed = onSongPressed,
                        onPlaylistPressed = onPlaylistPressed)
                }
            }
        }
    }
}