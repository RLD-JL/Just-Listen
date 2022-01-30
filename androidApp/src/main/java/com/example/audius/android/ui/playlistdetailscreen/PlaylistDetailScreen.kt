package com.example.audius.android.ui.playlistdetailscreen

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.audius.android.ui.playlistdetailscreen.components.BoxTopSection
import com.example.audius.android.ui.playlistdetailscreen.components.SongListScrollingSection
import com.example.audius.android.ui.playlistdetailscreen.components.TopSectionOverlay
import com.example.audius.android.ui.theme.modifiers.horizontalGradientBackground
import android.support.v4.media.MediaBrowserCompat
import androidx.compose.material.IconButton
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.exoplayer.utils.Constants
import com.example.audius.android.mapper.PlaylistDetailMapper
import com.example.audius.android.ui.loadingscreen.LoadingScreen
import com.example.audius.datalayer.models.SongIconList
import com.example.audius.datalayer.models.UserModel
import com.example.audius.viewmodel.interfaces.Item
import com.example.audius.viewmodel.screens.playlist.PlaylistItem
import com.example.audius.viewmodel.screens.playlistdetail.PlaylistDetailState
import kotlin.math.roundToInt

@Composable
fun PlaylistDetailScreen(
    playlistDetailState: PlaylistDetailState, onBackButtonPressed: (Boolean) -> Unit,
    musicServiceConnection: MusicServiceConnection,
    onSongPressed: (String, String, UserModel, SongIconList) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList) -> Unit,
    dominantColor: (Int) -> Unit
) {
    if (playlistDetailState.isLoading) {
        LoadingScreen()
    } else {
        val isPlayerReady: MutableState<Boolean> = remember {
            mutableStateOf(false)
        }

        val painter = rememberImagePainter(
            request = ImageRequest.Builder(context = LocalContext.current)
                .placeholder(ColorDrawable(MaterialTheme.colors.secondary.toArgb()))
                .data(playlistDetailState.playlistIcon).build()
        )

        val toolbarOffsetHeightPx = remember { mutableStateOf(0f) }

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    // try to consume before LazyColumn to collapse toolbar if needed, hence pre-scroll
                    val delta = available.y
                    val newOffset = toolbarOffsetHeightPx.value + delta
                    if (newOffset < 0)
                        toolbarOffsetHeightPx.value = newOffset
                    // here's the catch: let's pretend we consumed 0 in any case, since we want
                    // LazyColumn to scroll anyway for good UX
                    // We're basically watching scroll without taking it
                    return Offset.Zero
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {

            BottomScrollableContent(
                playlistDetailState = playlistDetailState,
                scrollState = toolbarOffsetHeightPx,
                nestedScrollConnection = nestedScrollConnection,
                playlist = playlistDetailState.songPlaylist,
                onShuffleClicked = {
                    playMusic(
                        musicServiceConnection,
                        playlistDetailState.songPlaylist,
                        isPlayerReady.value
                    )
                    isPlayerReady.value = true
                },
                onSongClicked = onSongPressed,
                onFavoritePressed = onFavoritePressed,
                dominantColor = dominantColor,
                painter = painter
            )
            AnimatedToolBar(playlistDetailState, toolbarOffsetHeightPx, onBackButtonPressed)
        }
    }
}

@Composable
fun AnimatedToolBar(
    playlistDetailState: PlaylistDetailState,
    scrollState: MutableState<Float>,
    onBackButtonPressed: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalGradientBackground(
                if (Dp(-scrollState.value) < 1080.dp)
                    listOf(
                        Color.Transparent,
                        Color.Transparent
                    ) else listOf(MaterialTheme.colors.background, MaterialTheme.colors.background)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        IconButton(onClick = { onBackButtonPressed(true) }) {
            Icon(
                imageVector = Icons.Default.ArrowBack, tint = MaterialTheme.colors.onSurface,
                contentDescription = null,
            )
        }
        Text(
            text = playlistDetailState.playlistName,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .padding(16.dp)
                .alpha(((-scrollState.value + 0.010f) / 1000).coerceIn(0f, 1f))
        )
        Icon(
            imageVector = Icons.Default.Search, tint = MaterialTheme.colors.onSurface,
            contentDescription = null
        )
    }
}

@Composable
fun BottomScrollableContent(
    playlistDetailState: PlaylistDetailState,
    scrollState: MutableState<Float>,
    playlist: List<PlaylistItem>,
    onSongClicked: (String, String, UserModel, SongIconList) -> Unit,
    onShuffleClicked: () -> Unit,
    nestedScrollConnection: NestedScrollConnection,
    onFavoritePressed: (String, String, UserModel, SongIconList) -> Unit,
    dominantColor: (Int) -> Unit,
    painter: ImagePainter
) {
    SongListScrollingSection(
        painter = painter,
        playlistDetailState = playlistDetailState,
        scrollState = scrollState,
        playlist = playlist,
        onSongClicked = onSongClicked,
        onShuffleClicked = onShuffleClicked,
        onFavoritePressed = onFavoritePressed,
        dominantColor = dominantColor
    )
}

fun playMusicFromId(
    musicServiceConnection: MusicServiceConnection,
    playlist: List<Item>,
    songId: String,
    isPlayerReady: Boolean
) {
    if (isPlayerReady) {
        musicServiceConnection.transportControls.playFromMediaId(songId, null)
    } else {
        playMusic(musicServiceConnection, playlist, isPlayerReady, songId)
    }
}

fun playMusic(
    musicServiceConnection: MusicServiceConnection,
    playlist: List<Item>,
    isPlayerReady: Boolean,
    playFromId: String = ""
) {
    if (!isPlayerReady) {
        musicServiceConnection.updatePlaylist(playlist)
        musicServiceConnection.subscribe(
            Constants.CLICKED_PLAYLIST,
            object : MediaBrowserCompat.SubscriptionCallback() {})
    }
    if (playFromId != "") {
        musicServiceConnection.transportControls.playFromMediaId(playFromId, null)
    }
}
