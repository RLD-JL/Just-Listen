package com.rld.justlisten.android.ui.searchscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.rld.justlisten.android.R
import com.rld.justlisten.android.ui.extensions.noRippleClickable
import com.rld.justlisten.android.ui.loadingscreen.LoadingScreen
import com.rld.justlisten.android.ui.playlistscreen.Header
import com.rld.justlisten.android.ui.playlistscreen.components.PlaylistRowItem
import com.rld.justlisten.android.ui.playlistscreen.components.TrackGridItem
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.viewmodel.interfaces.Item
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.screens.search.SearchScreenState
import com.rld.justlisten.viewmodel.screens.search.TrackItem
import com.rld.justlisten.android.ui.searchscreen.components.VerticalGrid

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
                onBackPressed,
                requester,
                onSearchPressed,
                searchScreenState,
                updateSearch = { updateSearch ->
                    searchFor = updateSearch.trimStart { it == '0' }
                },
                searchFor
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

@ExperimentalComposeUiApi
@Composable
fun AnimatedToolBar(
    onBackPressed: (Boolean) -> Unit,
    requester: FocusRequester,
    onSearchPressed: (String) -> Unit,
    searchedFor: SearchScreenState,
    updateSearch: (String) -> Unit,
    searchFor: String
) {

    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        IconButton(modifier = Modifier.weight(0.2f), onClick = { onBackPressed(true) }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
            )
        }
        TextField(modifier = Modifier
            .weight(0.6f)
            .focusRequester(requester),
            value = searchFor,
            singleLine = true,
            onValueChange = { newInput ->
                updateSearch(newInput)
            },
            label = {
                Text(text = "Search")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null
                )
            },
            keyboardActions = KeyboardActions(
                onSearch = {
                    onSearchPressed(searchFor)
                    keyboardController?.hide()
                }
            ),
        )
        IconButton(modifier = Modifier
            .weight(0.2f)
            .graphicsLayer {
                alpha = if (searchFor.isNotEmpty()) 1f else 0f
            },
            onClick = {
                updateSearch("")
                keyboardController?.show()
            }) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null
            )
        }
    }

    LaunchedEffect(Unit) {
        if (searchFor.isEmpty()) {
            requester.requestFocus()
        }
    }
}

@Composable
fun ShowPreviousSearches(listOfSearches: List<String>, onPreviousSearchedPressed: (String) ->Unit) {
    listOfSearches.fastForEach { itemSearched ->
        ItemRowSearch(itemSearched, onPreviousSearchedPressed)
    }
}

@Composable
fun ItemRowSearch(itemSearched: String, onPreviousSearchedPressed: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clickable(onClick = { onPreviousSearchedPressed(itemSearched) })
    ) {
        Icon(imageVector = Icons.Default.Search, contentDescription = null)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(modifier = Modifier.padding(start = 5.dp), text = itemSearched)
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_north_west_24),
                contentDescription = null
            )
        }
    }
}


@Composable
fun ShowSearchResults(
    searchResultTracks: List<TrackItem>,
    searchResultPlaylist: List<PlaylistItem>,
    onSongPressed: (String, String, String, SongIconList) -> Unit,
    onPlaylistPressed: (String, String, String, String, Boolean) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Header(text = "Top Find")
        SearchGridTracks(list = searchResultTracks, onSongPressed)
        Header(text = "Playlist", modifier = Modifier.padding(top = 10.dp))
        PlaylistResult(playlist = searchResultPlaylist, onPlaylistPressed)
    }
}

@Composable
fun SearchGridTracks(list: List<Item>, onSongPressed: (String, String, String, SongIconList) -> Unit) {
    VerticalGrid {
        list.fastForEach { item ->
            TrackGridItem(item, onSongPressed)
        }
    }
}

@Composable
fun PlaylistResult(
    playlist: List<PlaylistItem>,
    onPlaylistPressed: (String, String, String, String, Boolean) -> Unit) {
    LazyRow{
        itemsIndexed(items = playlist) {  _, playlistItem ->
            PlaylistRowItem(
                playlistItem = playlistItem,
                onPlaylistClicked = onPlaylistPressed)
        }
    }
}

