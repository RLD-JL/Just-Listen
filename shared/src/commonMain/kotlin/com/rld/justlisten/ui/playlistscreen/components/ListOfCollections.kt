package com.rld.justlisten.ui.playlistscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.rld.justlisten.datalayer.utils.Constants
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistState
import com.rld.justlisten.ui.theme.typography

@Composable
fun ListOfCollections(
    playlistState: PlaylistState,
    lasItemReached: (Int, PlayListEnum) -> Unit,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit,
    onSeeAllClicked: (String, PlayListEnum, String) -> Unit,
    onArtistClicked: (String, String) -> Unit,
    currentPlayingSongId: String? = null,
    currentlyPlayingPlaylistId: String? = null,
    isPlaying: Boolean = false
) {
    val list = remember {
        mutableListOf(
            "Trending Now",
            "Essential ${Constants.list[playlistState.queryIndex]}",
            "Hot ${Constants.list[playlistState.queryIndex2]}"
        )
    }
    list.fastForEachIndexed { index, item ->
        val playListEnum = when (index) {
            0 -> PlayListEnum.TOP_PLAYLIST
            1 -> PlayListEnum.REMIX
            else -> PlayListEnum.HOT
        }
        val query = when (index) {
            0 -> ""
            1 -> Constants.list[playlistState.queryIndex]
            else -> Constants.list[playlistState.queryIndex2]
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item,
                style = typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            val actionText = if (index == 0) "See all" else "Explore"
            Text(
                text = actionText,
                style = typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable {
                    onSeeAllClicked(item, playListEnum, query)
                }
            )
        }
        
        when (index) {
            0 -> PlaylistRow(
                playlist = playlistState.playlistItems,
                lasItemReached = lasItemReached,
                playlistEnum = PlayListEnum.TOP_PLAYLIST,
                onPlaylistClicked = onPlaylistClicked,
                lastIndexReached = playlistState.lastFetchPlaylist,
                onArtistClicked = onArtistClicked,
                currentPlayingSongId = currentPlayingSongId,
                currentlyPlayingPlaylistId = currentlyPlayingPlaylistId,
                isPlaying = isPlaying
            )
            1 -> PlaylistRow(
                playlist = playlistState.remixPlaylist,
                lasItemReached = lasItemReached,
                playlistEnum = PlayListEnum.REMIX,
                onPlaylistClicked = onPlaylistClicked,
                lastIndexReached = playlistState.lastFetchRemix,
                onArtistClicked = onArtistClicked,
                currentPlayingSongId = currentPlayingSongId,
                currentlyPlayingPlaylistId = currentlyPlayingPlaylistId,
                isPlaying = isPlaying
            )
            2 -> PlaylistRow(
                playlist = playlistState.hotPlaylist,
                lasItemReached = lasItemReached,
                playlistEnum = PlayListEnum.HOT,
                onPlaylistClicked = onPlaylistClicked,
                lastIndexReached = playlistState.lastFetchHot,
                onArtistClicked = onArtistClicked,
                currentPlayingSongId = currentPlayingSongId,
                currentlyPlayingPlaylistId = currentlyPlayingPlaylistId,
                isPlaying = isPlaying
            )
        }
    }
}
