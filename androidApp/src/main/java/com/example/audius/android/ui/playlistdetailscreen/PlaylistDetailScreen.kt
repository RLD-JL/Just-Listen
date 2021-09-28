package com.example.audius.android.ui.playlistdetailscreen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.example.audius.android.ui.playlistdetailscreen.components.BoxTopSection
import com.example.audius.android.ui.playlistdetailscreen.components.SongListScrollingSection
import com.example.audius.android.ui.playlistdetailscreen.components.TopSectionOverlay
import com.example.audius.android.ui.theme.modifiers.horizontalGradientBackground
import com.example.audius.android.ui.theme.modifiers.verticalGradientBackground
import com.example.audius.viewmodel.screens.trending.PlaylistDetailState
import android.graphics.drawable.BitmapDrawable
import android.support.v4.media.MediaBrowserCompat
import android.widget.Toast
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
import coil.ImageLoader
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.exoplayer.utils.Constants
import com.example.audius.android.ui.theme.graySurface
import com.example.audius.android.ui.theme.utils.ThemeMode
import com.example.audius.viewmodel.screens.trending.PlaylistItem
import kotlinx.coroutines.launch
import java.lang.Exception


@Composable
fun PlaylistDetailScreen(
    playlistDetailState: PlaylistDetailState, onBackButtonPressed: (Boolean) -> Unit,
    musicServiceConnection: MusicServiceConnection
) {
    val isPlayerReady: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }

    if (playlistDetailState.isLoading) {
        Toast.makeText(LocalContext.current, "LOADING SCREEN KEKW", Toast.LENGTH_SHORT).show()
    } else {

        val context = LocalContext.current
        val scrollState = rememberScrollState(0)
        val surfaceGradient = ThemeMode.spotifySurfaceGradient(isSystemInDarkTheme())

        val imageLoader = ImageLoader(context)
        val request = ImageRequest.Builder(context)
            .data(playlistDetailState.playlistIcon)
            .build()
        val imagePainter = rememberImagePainter(
            request = request,
            imageLoader = imageLoader
        )


        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            BoxTopSection(
                scrollState = scrollState,
                playlistDetailState = playlistDetailState,
                playlistPainter = imagePainter
            )
            TopSectionOverlay(scrollState = scrollState)
            BottomScrollableContent(playlist = playlistDetailState.songPlaylist,
                scrollState = scrollState,
                onShuffleClicked = {
                    playMusic(
                        musicServiceConnection,
                        playlistDetailState.songPlaylist,
                        isPlayerReady.value
                    )
                    isPlayerReady.value = true
                },
                onSongClicked = { songId ->
                    playMusicFromId(
                        musicServiceConnection,
                        playlistDetailState.songPlaylist,
                        songId,
                        isPlayerReady.value
                    )
                    isPlayerReady.value = true
                })
            AnimatedToolBar(playlistDetailState, scrollState, onBackButtonPressed)
        }
    }
}

@Composable
fun AnimatedToolBar(
    playlistDetailState: PlaylistDetailState,
    scrollState: ScrollState,
    onBackButtonPressed: (Boolean) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalGradientBackground(
                if (Dp(scrollState.value.toFloat()) < 1080.dp)
                    listOf(Color.Transparent, Color.Transparent) else listOf(MaterialTheme.colors.background,  MaterialTheme.colors.background)
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
                .alpha(((scrollState.value + 0.001f) / 1000).coerceIn(0f, 1f))
        )
        Icon(
            imageVector = Icons.Default.MoreVert, tint = MaterialTheme.colors.onSurface,
            contentDescription = null
        )
    }
}

@Composable
fun BottomScrollableContent(
    playlist: List<PlaylistItem>, scrollState: ScrollState, onSongClicked: (String) -> Unit,
    onShuffleClicked: () -> Unit
) {
    Column(modifier = Modifier.verticalScroll(state = scrollState)) {
        Spacer(modifier = Modifier.height(480.dp))
        Column(modifier = Modifier.horizontalGradientBackground(listOf(MaterialTheme.colors.background,  MaterialTheme.colors.background))) {
            SongListScrollingSection(
                playlist = playlist,
                onSongClicked = onSongClicked,
                onShuffleClicked = onShuffleClicked
            )
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

fun playMusicFromId(
    musicServiceConnection: MusicServiceConnection,
    playlist: List<PlaylistItem>,
    songId: String,
    isPlayerReady: Boolean
) {
    if (isPlayerReady) {
        musicServiceConnection.transportControls.playFromMediaId(songId, null)
    } else {
        playMusic(musicServiceConnection, playlist, isPlayerReady, songId, )
    }
}

fun playMusic(
    musicServiceConnection: MusicServiceConnection,
    playlist: List<PlaylistItem>,
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
