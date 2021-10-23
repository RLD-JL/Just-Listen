package com.example.audius.android.ui.playlistscreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audius.android.ui.loadingscreen.LoadingScreen
import com.example.audius.android.ui.playlistscreen.components.PlaylistRowItem
import com.example.audius.android.ui.playlistscreen.components.SpotifyHomeGridItem
import com.example.audius.android.ui.test.AlbumsDataProvider
import com.example.audius.android.ui.theme.typography
import com.example.audius.viewmodel.screens.playlist.PlayListEnum
import com.example.audius.viewmodel.screens.playlist.PlaylistItem
import com.example.audius.viewmodel.screens.playlist.PlaylistState
import com.guru.composecookbook.verticalgrid.VerticalGrid


@Composable
fun PlaylistScreen(
    lasItemReached: (Int, PlayListEnum) -> Unit,
    playlistState: PlaylistState,
    onPlaylistClicked: (String, String, String, String) -> Unit
) {
    if (playlistState.isLoading) {
        LoadingScreen()
    } else {
        val scrollState = rememberScrollState(0)
        Box(modifier = Modifier.fillMaxSize()) {
            ScrollableContent(
                lasItemReached = lasItemReached, scrollState = scrollState,
                playlistState = playlistState,
                onPlaylistClicked = onPlaylistClicked
            )
        }
    }
}

@Composable
fun ScrollableContent(
    lasItemReached: (Int, PlayListEnum) -> Unit,
    scrollState: ScrollState,
    playlistState: PlaylistState,
    onPlaylistClicked: (String, String, String, String) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        SpotifyTitle("Good Evening")
        //HomeGridSection()
        ListOfCollections(
            playlistState = playlistState, lasItemReached = lasItemReached,
            onPlaylistClicked = onPlaylistClicked
        )
        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
fun SpotifyTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = typography.h5.copy(fontWeight = FontWeight.ExtraBold),
        modifier = modifier.padding(start = 8.dp, end = 4.dp, bottom = 8.dp, top = 24.dp)
    )
}

@Composable
fun HomeGridSection() {
    val items = remember { AlbumsDataProvider.albums }
    VerticalGrid {
        items.take(6).forEach {
            SpotifyHomeGridItem(album = it)
        }
    }
}

@Composable
fun ListOfCollections(
    playlistState: PlaylistState, lasItemReached: (Int, PlayListEnum) -> Unit,
    onPlaylistClicked: (String, String, String, String) -> Unit
) {
    val list = remember {
        mutableListOf("Top Playlist", "Remix", "Yolo")
    }
    list.forEachIndexed { index, item ->
        SpotifyTitle(text = item)
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
    onPlaylistClicked: (String, String, String, String) -> Unit
) {
    LazyRow {
        itemsIndexed(items = playlist) { index, playlistItem ->

            if (index == playlist.size - 3)
                lasItemReached(index + 20, playlistEnum)

            PlaylistRowItem(
                playlistItem = playlistItem,
                onPlaylistClicked = onPlaylistClicked,

            )
        }
    }
}
