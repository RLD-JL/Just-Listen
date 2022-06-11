package com.rld.justlisten.android.ui.libraryscreen.components

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.rld.justlisten.android.ui.playlistscreen.components.PlaylistRowItem
import com.rld.justlisten.viewmodel.screens.library.LibraryState

@Composable
fun RowListOfRecentActivity(
    libraryState: LibraryState,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit,
    lastIndexReached: Boolean,
    lasItemReached: (Int) -> Unit
) {
    val fetchMore = remember { mutableStateOf(false) }

    LazyRow {
        itemsIndexed(items = libraryState.recentSongsItems) { index, playlistItem ->

            if (index == libraryState.recentSongsItems.lastIndex && !lastIndexReached) {
                LaunchedEffect(key1 = libraryState.recentSongsItems.lastIndex) {
                    lasItemReached(libraryState.recentSongsItems.size + 20)
                    fetchMore.value = true
                }
            }

            if (lastIndexReached) {
                fetchMore.value = false
            }

            PlaylistRowItem(
                playlistItem = playlistItem,
                onPlaylistClicked = onPlaylistClicked,
            )
        }
    }
}