package com.rld.justlisten.android.ui.playlistscreen

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.rld.justlisten.android.ui.loadingscreen.LoadingScreen
import com.rld.justlisten.android.ui.playlistscreen.components.PlaylistRowItem
import com.rld.justlisten.android.ui.theme.modifiers.horizontalGradientBackground
import com.rld.justlisten.android.ui.theme.typography
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.rld.justlisten.datalayer.utils.Constants.list
import com.rld.justlisten.viewmodel.Events
import com.rld.justlisten.viewmodel.screens.playlist.fetchPlaylist

@Composable
fun PlaylistScreen(
    playlistState: PlaylistState,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit,
    onSearchClicked: () -> Unit,
    refreshScreen: () -> Unit,
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
                        }
                    },
                    scrollState = scrollState,
                    playlistState = playlistState,
                    onPlaylistClicked = onPlaylistClicked,
                )
                AnimatedToolBar(onSearchClicked)
            }
        }
    }
}

@Composable
fun AnimatedToolBar(
    onSearchClicked: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalGradientBackground(
                listOf(MaterialTheme.colors.background, MaterialTheme.colors.background)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Header(text = "Good evening")
        Icon(
            modifier = Modifier.clickable(onClick = onSearchClicked),
            imageVector = Icons.Default.Search,
            contentDescription = null
        )
    }
}

@Composable
fun ScrollableContent(
    lasItemReached: (Int, PlayListEnum) -> Unit,
    scrollState: ScrollState,
    playlistState: PlaylistState,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        //HomeGridSection()
        ListOfCollections(
            playlistState = playlistState, lasItemReached = lasItemReached,
            onPlaylistClicked = onPlaylistClicked
        )
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun Header(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = typography.h5.copy(fontWeight = FontWeight.ExtraBold),
        modifier = modifier.padding(start = 8.dp, end = 4.dp, bottom = 8.dp, top = 24.dp)
    )
}

@Composable
fun ListOfCollections(
    playlistState: PlaylistState, lasItemReached: (Int, PlayListEnum) -> Unit,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit
) {
    val list = remember {
        mutableListOf("Top Playlist", list[playlistState.queryIndex], list[playlistState.queryIndex2])
    }
    list.fastForEachIndexed { index, item ->
        Header(text = item)
        when (index) {
            0 -> PlaylistRow(
                playlist = playlistState.playlistItems,
                lasItemReached = lasItemReached,
                PlayListEnum.TOP_PLAYLIST,
                onPlaylistClicked
            )
            1 -> PlaylistRow(
                playlist = playlistState.remixPlaylist,
                lasItemReached = lasItemReached,
                PlayListEnum.REMIX,
                onPlaylistClicked
            )
            2 -> PlaylistRow(
                playlist = playlistState.hotPlaylist,
                lasItemReached = lasItemReached,
                PlayListEnum.HOT,
                onPlaylistClicked
            )
        }
    }
}

@Composable
fun PlaylistRow(
    playlist: List<PlaylistItem>, lasItemReached: (Int, PlayListEnum) -> Unit,
    playlistEnum: PlayListEnum,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit,
) {
    val fetchMore = remember { mutableStateOf(false) }
    LazyRow(verticalAlignment = Alignment.CenterVertically) {
        itemsIndexed(items = playlist) { index, playlistItem ->

            if (index == playlist.size - 3) {
                lasItemReached(index + 20, playlistEnum)
                fetchMore.value = true
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
