package com.example.audius.android.ui.searchscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.audius.android.R
import com.example.audius.android.ui.extensions.noRippleClickable
import com.example.audius.android.ui.loadingscreen.LoadingScreen
import com.example.audius.android.ui.playlistscreen.Header
import com.example.audius.android.ui.playlistscreen.components.TrackGridItem
import com.example.audius.viewmodel.interfaces.Item
import com.example.audius.viewmodel.screens.search.SearchScreenState
import com.example.audius.viewmodel.screens.search.TrackItem
import com.guru.composecookbook.verticalgrid.VerticalGrid

@ExperimentalComposeUiApi
@Composable
fun SearchScreen(
    onBackPressed: (Boolean) -> Unit,
    onSearchPressed: (String) -> Unit,
    onSongPressed: (String) -> Unit,
    searchScreenState: SearchScreenState
) {
    val requester = FocusRequester()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState(0)

    Box(
        Modifier
            .fillMaxSize()
            .noRippleClickable(onClick = { focusManager.clearFocus() })
    ) {

        Column(
            Modifier
                .background(MaterialTheme.colors.background)
                .verticalScroll(scrollState)) {
            AnimatedToolBar(
                onBackPressed,
                requester,
                onSearchPressed,
                searchScreenState.searchFor
            )
            when {
                searchScreenState.isLoading -> {
                    LoadingScreen(5.dp)
                }
                searchScreenState.searchResultTracks.isEmpty() -> {
                    ShowPreviousSearches(searchScreenState.listOfSearches)
                }
                else -> {
                    ShowSearchResults(searchScreenState.searchResultTracks, onSongPressed)
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
    searchedFor: String
) {
    val inputField = remember { mutableStateOf(searchedFor) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        IconButton(modifier = Modifier.weight(0.2f), onClick = { onBackPressed(true) }) {
            Icon(
                imageVector = Icons.Default.ArrowBack, tint = MaterialTheme.colors.onSurface,
                contentDescription = null,
            )
        }
        TextField(modifier = Modifier
            .weight(0.6f)
            .focusRequester(requester),
            value = inputField.value,
            onValueChange = { newInput ->
                inputField.value = newInput
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
                    onSearchPressed(inputField.value)
                    keyboardController?.hide()
                }
            ),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = MaterialTheme.colors.background
            )
        )
        IconButton(modifier = Modifier
            .weight(0.2f)
            .graphicsLayer {
                alpha = if (inputField.value.isNotEmpty()) 1f else 0f
            },
            onClick = { inputField.value = ""
                        keyboardController?.show()}) {
            Icon(
                imageVector = Icons.Default.Close, tint = MaterialTheme.colors.onSurface,
                contentDescription = null
            )
        }
    }

    LaunchedEffect(Unit) {
        if (inputField.value.isEmpty()) {
            requester.requestFocus()
        }
    }
}

@Composable
fun ShowPreviousSearches(listOfSearches: List<String>) {
    listOfSearches.forEach { itemSearched ->
        ItemRowSearch(itemSearched)
    }
}

@Composable
fun ItemRowSearch(itemSearched: String) {
    Row(Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Icon(imageVector = Icons.Default.Search, contentDescription = null)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement  =  Arrangement.SpaceBetween) {
            Text(modifier = Modifier.padding(start = 5.dp), text = itemSearched)
            Icon(
                tint = MaterialTheme.colors.primary,
                painter = painterResource(id = R.drawable.ic_baseline_north_west_24), contentDescription = null)
        }
    }
}


@Composable
fun ShowSearchResults(searchResultTracks: List<TrackItem>, onSongPressed: (String) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Header(text = "Top Find")
        SearchGridTracks(list = searchResultTracks, onSongPressed)
    }
}


@Composable
fun SearchGridTracks(list: List<Item>, onSongPressed: (String) -> Unit) {
    VerticalGrid {
        list.forEach { item ->
            TrackGridItem(item, onSongPressed)
        }
    }
}