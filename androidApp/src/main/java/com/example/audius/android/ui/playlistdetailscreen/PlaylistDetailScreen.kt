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
import androidx.compose.ui.graphics.toArgb
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.exoplayer.utils.Constants
import com.example.audius.android.ui.loadingscreen.LoadingScreen
import com.example.audius.datalayer.models.SongIconList
import com.example.audius.datalayer.models.UserModel
import com.example.audius.viewmodel.interfaces.Item
import com.example.audius.viewmodel.screens.playlist.PlaylistItem
import com.example.audius.viewmodel.screens.playlistdetail.PlaylistDetailState

@Composable
fun PlaylistDetailScreen(
    playlistDetailState: PlaylistDetailState, onBackButtonPressed: (Boolean) -> Unit,
    musicServiceConnection: MusicServiceConnection,
    onSongPressed: (String, String, UserModel, SongIconList) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList) -> Unit
) {
    if (playlistDetailState.isLoading) {
        LoadingScreen()
    } else {
        val isPlayerReady: MutableState<Boolean> = remember {
            mutableStateOf(false)
        }
        val scrollState = rememberScrollState(0)

        val painter = rememberImagePainter(
            request = ImageRequest.Builder(context = LocalContext.current)
                .placeholder(ColorDrawable(MaterialTheme.colors.secondary.toArgb()))
                .data(playlistDetailState.playlistIcon).build()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            BoxTopSection(
                scrollState = scrollState,
                playlistDetailState = playlistDetailState,
                playlistPainter = painter
            )
            TopSectionOverlay(scrollState = scrollState)
            BottomScrollableContent(
                playlist = playlistDetailState.songPlaylist,
                scrollState = scrollState,
                onShuffleClicked = {
                    playMusic(
                        musicServiceConnection,
                        playlistDetailState.songPlaylist,
                        isPlayerReady.value
                    )
                    isPlayerReady.value = true
                },
                onSongClicked = onSongPressed,
                onFavoritePressed = onFavoritePressed
            )
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
                .alpha(((scrollState.value + 0.001f) / 1000).coerceIn(0f, 1f))
        )
        Icon(
            imageVector = Icons.Default.Search, tint = MaterialTheme.colors.onSurface,
            contentDescription = null
        )
    }
}

@Composable
fun BottomScrollableContent(
    playlist: List<PlaylistItem>,
    scrollState: ScrollState,
    onSongClicked: (String, String, UserModel, SongIconList) -> Unit,
    onShuffleClicked: () -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList) -> Unit
) {
    Column(modifier = Modifier.verticalScroll(scrollState)) {
        Spacer(modifier = Modifier.height(480.dp))
        Column(
            modifier = Modifier.horizontalGradientBackground(
                listOf(
                    MaterialTheme.colors.background,
                    MaterialTheme.colors.background
                )
            )
        ) {
            SongListScrollingSection(
                playlist = playlist,
                onSongClicked = onSongClicked,
                onShuffleClicked = onShuffleClicked,
                onFavoritePressed = onFavoritePressed
            )
        }
    }
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
