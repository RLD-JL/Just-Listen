package com.rld.justlisten.android.ui.playlistscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.rld.justlisten.android.ui.loadingscreen.LoadingScreen
import com.rld.justlisten.android.ui.playlistscreen.components.AnimatedToolBar
import com.rld.justlisten.android.ui.playlistscreen.components.ScrollableContent
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.utils.Constants.list
import com.rld.justlisten.viewmodel.Events
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistState
import com.rld.justlisten.viewmodel.screens.playlist.fetchPlaylist
import com.rld.justlisten.viewmodel.screens.playlist.getNewTracks

@Composable
fun PlaylistScreen(
    playlistState: PlaylistState,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit,
    onSearchClicked: () -> Unit,
    refreshScreen: () -> Unit,
    onSongPressed: (String, String, String, SongIconList) -> Unit,
    events: Events
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
                            PlayListEnum.TOP_PLAYLIST -> events.fetchPlaylist(
                                lastIndex,
                                PlayListEnum.TOP_PLAYLIST
                            )
                            PlayListEnum.REMIX -> events.fetchPlaylist(
                                lastIndex,
                                PlayListEnum.REMIX,
                                list[playlistState.queryIndex]
                            )
                            PlayListEnum.HOT -> events.fetchPlaylist(
                                lastIndex,
                                PlayListEnum.HOT,
                                list[playlistState.queryIndex2]
                            )
                            PlayListEnum.CURRENT_PLAYLIST -> TODO()
                            PlayListEnum.FAVORITE -> TODO()
                            PlayListEnum.CREATED_BY_USER -> TODO()
                            PlayListEnum.MOST_PLAYED -> TODO()
                        }
                    },
                    scrollState = scrollState,
                    playlistState = playlistState,
                    onPlaylistClicked = onPlaylistClicked,
                    onSongPressed = onSongPressed,
                    getNewTracks = { category, timeRange ->
                        events.getNewTracks(category, timeRange)
                    }
                )
                AnimatedToolBar(onSearchClicked)
            }
        }
    }
}
