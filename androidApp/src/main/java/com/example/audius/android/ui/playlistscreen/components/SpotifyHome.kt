package com.example.audius.android.ui.playlistscreen.components

import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.test.AlbumsDataProvider
import com.example.audius.android.ui.theme.modifiers.horizontalGradientBackground
import com.example.audius.viewmodel.screens.trending.PlayListEnum
import com.example.audius.viewmodel.screens.trending.PlaylistItem
import com.example.audius.viewmodel.screens.trending.PlaylistState
import com.example.audius.viewmodel.screens.trending.TrendingListState
import com.guru.composecookbook.spotify.ui.home.components.SpotifyHomeGridItem
import com.guru.composecookbook.verticalgrid.VerticalGrid


val graySurface = Color(0xFF2A2A2A)

fun spotifySurfaceGradient(isDark: Boolean) =
    if (isDark) listOf(graySurface, Color.Black) else listOf(Color.White, Color.LightGray)


@Composable
fun SpotifyHome(
    lasItemReached: (Int, PlayListEnum) -> Unit,
    playlistState: PlaylistState,
    musicServiceConnection: MusicServiceConnection,
    onPlaylistClicked:(String) ->Unit
) {
    val scrollState = rememberScrollState(0)
    val surfaceGradient = spotifySurfaceGradient(isSystemInDarkTheme())
    Box(modifier = Modifier.fillMaxSize()) {
        ScrollableContent(lasItemReached = lasItemReached, scrollState = scrollState,
            surfaceGradient = surfaceGradient, playlistState = playlistState,
        musicServiceConnection = musicServiceConnection, onPlaylistClicked = onPlaylistClicked)
        Icon(
            imageVector = Icons.Outlined.Settings,
            tint = MaterialTheme.colors.onSurface,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(start = 12.dp, end = 12.dp, top = 36.dp, bottom = 12.dp)
                .alpha(animateFloatAsState(1f - scrollState.value / 200f).value)
        )
      //  PlayerBottomBar(Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun ScrollableContent(lasItemReached: (Int, PlayListEnum) -> Unit, scrollState: ScrollState,
                      surfaceGradient: List<Color>, playlistState: PlaylistState,
                      musicServiceConnection: MusicServiceConnection, onPlaylistClicked:(String) ->Unit) {
    Column(
        modifier = Modifier
            .horizontalGradientBackground(surfaceGradient)
            .padding(8.dp)
            .verticalScroll(state = scrollState)
    ) {
        Spacer(modifier = Modifier.height(50.dp))
        //SpotifyTitle("Good Evening")
        //HomeGridSection()
        HomeLanesSection(playlistState = playlistState, lasItemReached = lasItemReached,
            musicServiceConnection = musicServiceConnection, onPlaylistClicked = onPlaylistClicked)
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
fun HomeLanesSection(playlistState: PlaylistState, lasItemReached: (Int, PlayListEnum) ->Unit,
                     musicServiceConnection: MusicServiceConnection,
                     onPlaylistClicked:(String) ->Unit)
{
        val list = mutableListOf("Top Playlist", "Remix")
       list.forEachIndexed { index, item->
           SpotifyTitle(text = item)
           if (index==0)
           SpotifyLane(playlistState.playlistItems, lasItemReached, PlayListEnum.TOP_PLAYLIST, musicServiceConnection,onPlaylistClicked)
           else
               SpotifyLane(playlist = playlistState.remixPlaylist, lasItemReached = lasItemReached, PlayListEnum.REMIX, musicServiceConnection, onPlaylistClicked)
       }
}

@Composable
fun SpotifyLane(playlist: List<PlaylistItem>, lasItemReached: (Int, PlayListEnum) ->Unit,
                playlistEnum: PlayListEnum, musicServiceConnection: MusicServiceConnection,
                onPlaylistClicked:(String)->Unit) {
    LazyRow {
        itemsIndexed(items = playlist) { index, playlistItem->
            if (index == playlist.size - 1)
                    lasItemReached(index+20, playlistEnum)
           SpotifyLaneItem(playlistItem = playlistItem, musicServiceConnection, onPlaylistClicked)
        }
    }
}

@Preview
@Composable
fun PreviewSpotifyHome() {
    PreviewSpotifyHome()
}