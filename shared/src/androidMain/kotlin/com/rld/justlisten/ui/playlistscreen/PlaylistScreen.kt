package com.rld.justlisten.ui.playlistscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.utils.Constants.list
import com.rld.justlisten.ui.loadingscreen.LoadingScreen
import com.rld.justlisten.ui.playlistscreen.components.AnimatedToolBar
import com.rld.justlisten.ui.playlistscreen.components.ScrollableContent
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistState
import com.rld.justlisten.viewmodel.screens.playlist.TimeRange
import com.rld.justlisten.viewmodel.screens.playlist.TracksCategory

@Composable
fun PlaylistScreen(
    playlistState: PlaylistState,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit,
    onSearchClicked: () -> Unit,
    refreshScreen: () -> Unit,
    onSongPressed: (String, String, String, SongIconList) -> Unit,
    fetchPlaylist: (Int, PlayListEnum, String) -> Unit,
    getNewTracks: (TracksCategory, TimeRange) -> Unit,
) {
    if (playlistState.isLoading) {
        LoadingScreen()
    } else {
        val scrollState = rememberScrollState(0)
        val swipeRefreshState = rememberSwipeRefreshState(false)
        SwipeRefresh(state = swipeRefreshState, onRefresh = refreshScreen) {
            Box(modifier = Modifier.fillMaxSize()) {
                ScrollableContent(
                    lasItemReached = { lastIndex, playListEnum ->
                        when (playListEnum) {
                            PlayListEnum.TOP_PLAYLIST -> fetchPlaylist(
                                lastIndex,
                                PlayListEnum.TOP_PLAYLIST,
                                "Rock",
                            )
                            PlayListEnum.REMIX -> fetchPlaylist(
                                lastIndex,
                                PlayListEnum.REMIX,
                                list[playlistState.queryIndex],
                            )
                            PlayListEnum.HOT -> fetchPlaylist(
                                lastIndex,
                                PlayListEnum.HOT,
                                list[playlistState.queryIndex2],
                            )
                            else -> Unit
                        }
                    },
                    scrollState = scrollState,
                    playlistState = playlistState,
                    onPlaylistClicked = onPlaylistClicked,
                    onSongPressed = onSongPressed,
                    getNewTracks = getNewTracks,
                )
                AnimatedToolBar(onSearchClicked)
            }
        }
    }
}
