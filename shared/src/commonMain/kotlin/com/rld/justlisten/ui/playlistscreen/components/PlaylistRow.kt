package com.rld.justlisten.ui.playlistscreen.components

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rld.justlisten.ui.components.MusicLoadingSpinner
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
    lastIndexReached: Boolean = false,
    onArtistClicked: ((String, String) -> Unit)? = null,
    currentPlayingSongId: String? = null,
    currentlyPlayingPlaylistId: String? = null,
    isPlaying: Boolean = false
) {
    val fetchMore = remember { mutableStateOf(false) }
    LazyRow(verticalAlignment = Alignment.CenterVertically) {
        itemsIndexed(items = playlist, key = { _, item -> item.id }) { index, playlistItem ->

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

            val isPlayingThisPlaylist = isPlaying &&
                    (currentlyPlayingPlaylistId == playlistItem.id || currentPlayingSongId == playlistItem.id)

            PlaylistRowItem(
                playlistItem = playlistItem,
                onPlaylistClicked = onPlaylistClicked,
                onArtistClicked = onArtistClicked,
                isPlaying = isPlayingThisPlaylist
            )
        }
        if (fetchMore.value) {
            item {
                MusicLoadingSpinner(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    size = 32.dp
                )
            }
        }
    }
}
