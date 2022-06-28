package com.rld.justlisten.android.ui.playlistscreen.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.util.fastForEachIndexed
import com.rld.justlisten.datalayer.utils.Constants
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistState

@Composable
fun ListOfCollections(
    playlistState: PlaylistState, lasItemReached: (Int, PlayListEnum) -> Unit,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit
) {
    val list = remember {
        mutableListOf(
            "Top Playlist",
            Constants.list[playlistState.queryIndex],
            Constants.list[playlistState.queryIndex2]
        )
    }
    list.fastForEachIndexed { index, item ->
        Header(text = item)
        when (index) {
            0 -> PlaylistRow(
                playlist = playlistState.playlistItems,
                lasItemReached = lasItemReached,
                PlayListEnum.TOP_PLAYLIST,
                onPlaylistClicked,
                playlistState.lastFetchPlaylist
            )
            1 -> PlaylistRow(
                playlist = playlistState.remixPlaylist,
                lasItemReached = lasItemReached,
                PlayListEnum.REMIX,
                onPlaylistClicked,
                playlistState.lastFetchRemix
            )
            2 -> PlaylistRow(
                playlist = playlistState.hotPlaylist,
                lasItemReached = lasItemReached,
                PlayListEnum.HOT,
                onPlaylistClicked,
                playlistState.lastFetchHot
            )
        }
    }
}