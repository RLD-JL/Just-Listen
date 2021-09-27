package com.example.audius.android.ui.playlistscreen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audius.android.ui.playlistscreen.components.PlaylistRowItem
import com.example.audius.android.ui.playlistscreen.components.SpotifyHomeGridItem
import com.example.audius.android.ui.test.AlbumsDataProvider
import com.example.audius.android.ui.theme.modifiers.horizontalGradientBackground
import com.example.audius.android.ui.theme.typography
import com.example.audius.android.ui.theme.utils.ThemeMode
import com.example.audius.viewmodel.screens.trending.PlayListEnum
import com.example.audius.viewmodel.screens.trending.PlaylistItem
import com.example.audius.viewmodel.screens.trending.PlaylistState
import com.guru.composecookbook.verticalgrid.VerticalGrid


@Composable
fun PlaylistScreen(
    lasItemReached: (Int, PlayListEnum) -> Unit,
    playlistState: PlaylistState,
    onPlaylistClicked:(String, String, String, String) ->Unit
) {
    val scrollState = rememberScrollState(0)
    val surfaceGradient = ThemeMode.spotifySurfaceGradient(isSystemInDarkTheme())
    Box(modifier = Modifier.fillMaxSize(1f)) {
        ScrollableContent(lasItemReached = lasItemReached, scrollState = scrollState,
            surfaceGradient = surfaceGradient, playlistState = playlistState,
         onPlaylistClicked = onPlaylistClicked)
        Icon(
            imageVector = Icons.Outlined.Settings,
            tint = MaterialTheme.colors.onSurface,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(start = 12.dp, end = 12.dp, top = 36.dp, bottom = 12.dp)
                .alpha(animateFloatAsState(1f - scrollState.value / 200f).value)
        )
    }
}

@Composable
fun ScrollableContent(lasItemReached: (Int, PlayListEnum) -> Unit, scrollState: ScrollState,
                      surfaceGradient: List<Color>, playlistState: PlaylistState,
                      onPlaylistClicked:(String, String, String, String) ->Unit) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(state = scrollState)
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        //SpotifyTitle("Good Evening")
        //HomeGridSection()
        ListOfCollections(playlistState = playlistState, lasItemReached = lasItemReached,
            onPlaylistClicked = onPlaylistClicked)
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
fun ListOfCollections(playlistState: PlaylistState, lasItemReached: (Int, PlayListEnum) ->Unit,
                     onPlaylistClicked:(String, String, String, String) ->Unit)
{
        val list = mutableListOf("Top Playlist", "Remix")
       list.forEachIndexed { index, item->
           SpotifyTitle(text = item)
           if (index==0)
           PlaylistRow(playlistState.playlistItems, lasItemReached, PlayListEnum.TOP_PLAYLIST,
               onPlaylistClicked = onPlaylistClicked)
           else
               PlaylistRow(playlist = playlistState.remixPlaylist, lasItemReached = lasItemReached, PlayListEnum.REMIX, onPlaylistClicked)
       }
}

@Composable
fun PlaylistRow(playlist: List<PlaylistItem>, lasItemReached: (Int, PlayListEnum) ->Unit,
                playlistEnum: PlayListEnum,
                onPlaylistClicked:(String, String, String, String)->Unit) {
    LazyRow {
        itemsIndexed(items = playlist) { index, playlistItem->
            if (index == playlist.size - 1)
                    lasItemReached(index+20, playlistEnum)
           PlaylistRowItem(playlistItem = playlistItem,
               onPlaylistClicked = onPlaylistClicked)
        }
    }
}
