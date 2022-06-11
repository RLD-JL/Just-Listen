package com.rld.justlisten.android.ui.searchscreen.components

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import com.rld.justlisten.android.ui.playlistscreen.components.PlaylistRowItem
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

@Composable
fun PlaylistResult(
    playlist: List<PlaylistItem>,
    onPlaylistPressed: (String, String, String, String, Boolean) -> Unit) {
    LazyRow {
        itemsIndexed(items = playlist) { _, playlistItem ->
            PlaylistRowItem(
                playlistItem = playlistItem,
                onPlaylistClicked = onPlaylistPressed
            )
        }
    }
}