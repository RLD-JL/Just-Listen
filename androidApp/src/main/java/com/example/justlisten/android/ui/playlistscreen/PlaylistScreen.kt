package com.example.justlisten.android.ui.playlistscreen

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
import com.example.justlisten.android.ui.loadingscreen.LoadingScreen
import com.example.justlisten.android.ui.playlistscreen.components.PlaylistRowItem
import com.example.justlisten.android.ui.theme.modifiers.horizontalGradientBackground
import com.example.justlisten.android.ui.theme.typography
import com.example.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.example.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.example.justlisten.viewmodel.screens.playlist.PlaylistState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun PlaylistScreen(
    lasItemReached: (Int, PlayListEnum) -> Unit,
    playlistState: PlaylistState,
    onPlaylistClicked: (String, String, String, String) -> Unit,
    onSearchClicked: () -> Unit,
    refreshScreen: () ->Unit
) {
    if (playlistState.isLoading) {
        LoadingScreen()
    } else {
        val scrollState = rememberScrollState(0)
        val swipeRefreshState = rememberSwipeRefreshState(false)
        SwipeRefresh(state = swipeRefreshState, onRefresh = refreshScreen) {
        Box(modifier = Modifier.fillMaxSize()) {
            ScrollableContent(
                lasItemReached = lasItemReached, scrollState = scrollState,
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
            imageVector = Icons.Default.Search, tint = MaterialTheme.colors.onSurface,
            contentDescription = null
        )
    }
}

@Composable
fun ScrollableContent(
    lasItemReached: (Int, PlayListEnum) -> Unit,
    scrollState: ScrollState,
    playlistState: PlaylistState,
    onPlaylistClicked: (String, String, String, String) -> Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        //HomeGridSection()
        ListOfCollections(
            playlistState = playlistState, lasItemReached = lasItemReached,
            onPlaylistClicked = onPlaylistClicked)
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
    onPlaylistClicked: (String, String, String, String) -> Unit) {
    val list = remember {
        mutableListOf("Top Playlist", "Remix", "Yolo")
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
                playlist = playlistState.remixPlaylist,
                lasItemReached = lasItemReached,
                PlayListEnum.REMIX,
                onPlaylistClicked
                )
        }
    }
}

@Composable
fun PlaylistRow(
    playlist: List<PlaylistItem>, lasItemReached: (Int, PlayListEnum) -> Unit,
    playlistEnum: PlayListEnum,
    onPlaylistClicked: (String, String, String, String) -> Unit, ) {
    val fetchMore = remember { mutableStateOf(false)}
    LazyRow(verticalAlignment =  Alignment.CenterVertically) {
        itemsIndexed(items = playlist) { index, playlistItem ->

            if (index == playlist.size - 3) {
                lasItemReached(index + 20, playlistEnum)
                fetchMore.value = true
            }

            PlaylistRowItem(
                playlistItem = playlistItem,
                onPlaylistClicked = onPlaylistClicked)
        }
        if (fetchMore.value) {
           item{
               CircularProgressIndicator()
           }
        }
    }
}
