package com.example.audius.android.ui.searchscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.audius.android.ui.extensions.noRippleClickable
import com.example.audius.android.ui.loadingscreen.LoadingScreen
import com.example.audius.viewmodel.screens.search.SearchScreenState

@ExperimentalComposeUiApi
@Composable
fun SearchScreen(onBackPressed: (Boolean) -> Unit,
onSearchPressed: (String) ->Unit,
searchScreenState: SearchScreenState) {
    val requester = FocusRequester()
    val focusManager = LocalFocusManager.current
    if (searchScreenState.isLoading) {
        LoadingScreen()
    } else {
        Box(
            Modifier
                .fillMaxSize()
                .noRippleClickable(onClick = { focusManager.clearFocus() })
        ) {
            Column(Modifier.background(Color.Black)) {
                AnimatedToolBar(onBackPressed, requester, onSearchPressed)
                ShowPreviousSearches(searchScreenState.listOfSearches)
            }
        }
    }
}

@ExperimentalComposeUiApi
@Composable
fun AnimatedToolBar(
    onBackPressed: (Boolean) -> Unit,
    requester: FocusRequester,
    onSearchPressed: (String) -> Unit
) {
    val inputField = remember { mutableStateOf("") }
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
                backgroundColor =  MaterialTheme.colors.background
            )
        )
        IconButton(modifier = Modifier
            .weight(0.2f)
            .graphicsLayer {
                alpha = if (inputField.value.isNotEmpty()) 1f else 0f
            },
            onClick = { inputField.value = "" }) {
            Icon(
                imageVector = Icons.Default.Close, tint = MaterialTheme.colors.onSurface,
                contentDescription = null
            )
        }
    }

    LaunchedEffect(Unit) {
        requester.requestFocus()
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
    Row(Modifier.fillMaxWidth()) {
        Icon(imageVector = Icons.Default.Search, contentDescription = null)
        Text(text = itemSearched)
        Icon(imageVector = Icons.Default.Share, contentDescription = null)
    }
}
