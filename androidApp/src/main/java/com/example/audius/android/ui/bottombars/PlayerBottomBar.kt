package com.example.audius.android.ui.bottombars

import android.media.session.PlaybackState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.audius.android.R
import com.example.audius.android.exoplayer.MusicService.Companion.curSongDuration
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.theme.typography
import com.example.audius.android.ui.utils.*

@Composable
fun PlayerBottomBar(
    currentFraction: Float,
    onSkipNextPressed: () -> Unit,
    musicServiceConnection: MusicServiceConnection,
) {
    val songIcon =
        musicServiceConnection.currentPlayingSong.value?.description?.iconUri.toString()
    val title =
        musicServiceConnection.currentPlayingSong.value?.description?.title.toString()

    PlayBar(currentFraction, songIcon, title, musicServiceConnection, onSkipNextPressed)

}

@Composable
fun PlayBar(
    currentFraction: Float,
    songIcon: String,
    title: String,
    musicServiceConnection: MusicServiceConnection,
    onSkipNextPressed: () -> Unit,
) {
    BoxWithConstraints {
        Column(Modifier.fillMaxSize()) {
            val constraints = this@BoxWithConstraints
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colors.primaryVariant),
            ) {

                Image(
                    painter = rememberImagePainter(songIcon),
                    modifier = Modifier
                        .size(width = widthSize(currentFraction).dp,
                            height =  heightSize(currentFraction).dp)
                        .offset(x = offsetX(currentFraction,constraints.maxWidth.value).dp,
                                y = offsetY(currentFraction, constraints.maxHeight.value).dp),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds
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
                        painter = painterResource(id = R.drawable.exo_icon_play),
                        modifier = Modifier
                            .padding(8.dp)
                            .clickable(
                                onClick = { musicServiceConnection.transportControls.play() }
                            ),
                        contentDescription = null
                    )
                }
                if (musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PLAYING) {
                    Icon(
                        painter = painterResource(id = R.drawable.exo_icon_pause),
                        modifier = Modifier
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
            LinearProgressIndicator(
                progress = musicServiceConnection.songDuration.value / curSongDuration.toFloat(),
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
            )
        }
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
