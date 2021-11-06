package com.example.audius.android.ui.bottombars

import android.media.session.PlaybackState
import android.service.quicksettings.Tile
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.audius.android.R
import com.example.audius.android.exoplayer.MusicService.Companion.curSongDuration
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.SheetState
import com.example.audius.android.ui.isScrolled
import com.example.audius.android.ui.theme.typography
import com.example.audius.android.ui.utils.lerp

@Composable
fun PlayerBottomBar(
    onSkipNextPressed: () -> Unit,
    musicServiceConnection: MusicServiceConnection,
) {
    if (musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PLAYING
        || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PAUSED
        || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_SKIPPING_TO_NEXT
        || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_BUFFERING
        || musicServiceConnection.currentPlayingSong.value != null
    ) {
        val songIcon =
            musicServiceConnection.currentPlayingSong.value?.description?.iconUri.toString()
        val title =
            musicServiceConnection.currentPlayingSong.value?.description?.title.toString()

            PlayBar(songIcon, title, musicServiceConnection, onSkipNextPressed)
            LinearProgressIndicator(
                progress = musicServiceConnection.songDuration.value / curSongDuration.toFloat(),
                Modifier
                    .fillMaxWidth()
                    .height(1.dp))
        Text("", modifier = Modifier.fillMaxSize())
    }
}


@Composable
fun PlayBar(
    songIcon: String,
    title: String,
    musicServiceConnection: MusicServiceConnection,
    onSkipNextPressed: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colors.primaryVariant),
    ) {
        Image(
            painter = rememberImagePainter(songIcon),
            modifier = Modifier.size(50.dp),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        Text(
            text = title,
            style = typography.h6.copy(fontSize = 10.sp),
            modifier = Modifier
                .padding(8.dp)
                .weight(1f),
        )
        Icon(
            imageVector = Icons.Default.FavoriteBorder, modifier = Modifier.padding(8.dp),
            contentDescription = null
        )
        if (musicServiceConnection.playbackState.value?.state != PlaybackState.STATE_PLAYING &&
            musicServiceConnection.playbackState.value?.state != PlaybackState.STATE_BUFFERING
        ) {
            Icon(
                painter = painterResource(id = R.drawable.exo_icon_play), modifier = Modifier
                    .padding(8.dp)
                    .clickable(
                        onClick = { musicServiceConnection.transportControls.play() }
                    ),
                contentDescription = null
            )
        }
        if (musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PLAYING) {
            Icon(
                painter = painterResource(id = R.drawable.exo_icon_pause), modifier = Modifier
                    .padding(8.dp)
                    .clickable(
                        onClick = { musicServiceConnection.transportControls.pause() }
                    ),
                contentDescription = null
            )
        }
        IsLoading(musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_BUFFERING)

        Icon(
            painter = painterResource(id = R.drawable.exo_ic_skip_next),
            modifier = Modifier
                .padding(8.dp)
                .clickable(onClick = onSkipNextPressed),
            contentDescription = null,
        )
    }
}

@Composable
fun IsLoading(isLoading: Boolean) {
    if (isLoading) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(24.dp * 2f)
        ) {
            CircularProgressIndicator()
            Icon(
                painter = painterResource(id = R.drawable.exo_icon_pause), modifier = Modifier,
                contentDescription = null
            )
        }
    }
}
