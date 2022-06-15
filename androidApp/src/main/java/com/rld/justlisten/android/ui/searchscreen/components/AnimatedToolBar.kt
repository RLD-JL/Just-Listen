package com.rld.justlisten.android.ui.searchscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType

@ExperimentalComposeUiApi
@Composable
fun AnimatedToolBar(
    onBackPressed: (Boolean) -> Unit,
    requester: FocusRequester,
    onSearchPressed: (String) -> Unit,
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
        TextField(
            modifier = Modifier
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