package com.rld.justlisten.ui.playlistscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.utils.Constants.list
import com.rld.justlisten.ui.loadingscreen.LoadingScreen
import com.rld.justlisten.ui.playlistscreen.components.AnimatedToolBar
import com.rld.justlisten.ui.playlistscreen.components.ScrollableContent
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistState
import com.rld.justlisten.viewmodel.screens.playlist.TimeRange
import com.rld.justlisten.viewmodel.screens.playlist.TracksCategory

import com.rld.justlisten.ui.actions.PlaylistScreenAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    playlistState: PlaylistState,
    onAction: (PlaylistScreenAction) -> Unit
) {
    if (playlistState.isLoading) {
        LoadingScreen()
    } else {
        val scrollState = rememberScrollState(0)
        PullToRefreshBox(
            isRefreshing = playlistState.isLoading,
            onRefresh = { onAction(PlaylistScreenAction.RefreshScreen) },
            modifier = Modifier.fillMaxSize()
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                ScrollableContent(
                    lasItemReached = { lastIndex, playListEnum ->
                        when (playListEnum) {
                            PlayListEnum.TOP_PLAYLIST -> onAction(PlaylistScreenAction.FetchMorePlaylists(
                                lastIndex,
                                PlayListEnum.TOP_PLAYLIST,
                                "Rock",
                            ))
                            PlayListEnum.REMIX -> onAction(PlaylistScreenAction.FetchMorePlaylists(
                                lastIndex,
                                PlayListEnum.REMIX,
                                list[playlistState.queryIndex],
                            ))
                            PlayListEnum.HOT -> onAction(PlaylistScreenAction.FetchMorePlaylists(
                                lastIndex,
                                PlayListEnum.HOT,
                                list[playlistState.queryIndex2],
                            ))
                            else -> Unit
                        }
                    },
                    scrollState = scrollState,
                    playlistState = playlistState,
                    onAction = onAction,
                )
            }
        }
    }
}
