package com.rld.justlisten.android.ui.playlistscreen.components

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

@Composable
fun PlaylistRow(
    playlist: List<PlaylistItem>, lasItemReached: (Int, PlayListEnum) -> Unit,
    playlistEnum: PlayListEnum,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit,
    lastIndexReached: Boolean = false
) {
    val fetchMore = remember { mutableStateOf(false) }
    LazyRow(verticalAlignment = Alignment.CenterVertically) {
        itemsIndexed(items = playlist) { index, playlistItem ->

            if (index == playlist.lastIndex && !lastIndexReached) {
                LaunchedEffect(key1 = playlist.lastIndex)
                {
                    lasItemReached(index + 20, playlistEnum)
                    fetchMore.value = true
                }
            }

            if (lastIndexReached) {
                fetchMore.value = false
            }

            PlaylistRowItem(
                playlistItem = playlistItem,
                onPlaylistClicked = onPlaylistClicked
            )
        }
        if (fetchMore.value) {
            item {
                CircularProgressIndicator()
            }
        }
    }
}