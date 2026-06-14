package com.rld.justlisten.ui.libraryscreen.components

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.rld.justlisten.ui.playlistscreen.components.PlaylistRowItem
import com.rld.justlisten.viewmodel.screens.library.LibraryState
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

@Composable
fun RowListOfRecentActivity(
    libraryState: LibraryState,
    onSongClicked: (PlaylistItem) -> Unit,
    lastIndexReached: Boolean,
    lasItemReached: (Int) -> Unit,
    onArtistClicked: (String, String) -> Unit
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
                onPlaylistClicked = { _, _, _, _, _ -> onSongClicked(playlistItem) },
                onArtistClicked = onArtistClicked
            )
        }
    }
}
