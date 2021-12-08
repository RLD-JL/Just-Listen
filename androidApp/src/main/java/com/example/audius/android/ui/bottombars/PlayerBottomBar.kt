package com.example.audius.android.ui.bottombars

import android.media.session.PlaybackState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.audius.android.R
import com.example.audius.android.exoplayer.MusicService.Companion.curSongDuration
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.theme.modifiers.horizontalGradientBackground
import com.example.audius.android.ui.theme.modifiers.verticalGradientBackground
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
    val dominantListOfColor = remember{mutableMapOf<String, List<Color>>()}
    val list = dominantListOfColor[title]
    BoxWithConstraints(modifier = if(list?.isNotEmpty() == true) Modifier.verticalGradientBackground(list) else Modifier) {
        Column(Modifier.fillMaxSize()) {
            val constraints = this@BoxWithConstraints
            Row(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val painter = rememberImagePainter(
                    request = ImageRequest.Builder(context = LocalContext.current)
                        .data(songIcon).allowHardware(false).build(),
                    onExecute = { previous, current ->
                        (widthSize(currentFraction) == 300f) || previous?.request?.data != current.request.data
                    }
                )
                (painter.state as? ImagePainter.State.Success)?.let {
                    successState ->
                    LaunchedEffect(Unit) {
                        val drawable = successState.result.drawable
                        Palette.Builder(drawable.toBitmap()).generate {palette ->
                            palette?.dominantSwatch?.let {
                                dominantListOfColor[title] = listOf(Color(it.rgb), Color(it.rgb).copy(alpha = 0.6f))
                            }
                        }
                    }
                }
                Image(
                    painter = painter,
                    modifier = Modifier
                        .size(
                            width = widthSize(currentFraction).dp,
                            height = heightSize(currentFraction).dp
                        )
                        .offset(
                            x = offsetX(currentFraction, constraints.maxWidth.value).dp,
                            y = offsetY(currentFraction, constraints.maxHeight.value).dp
                        ),
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds
                )
                PlayBarActionsMinimized(
                    currentFraction,
                    musicServiceConnection,
                    title,
                    onSkipNextPressed
                )
            }
            LinearProgressIndicator(
                progress = musicServiceConnection.songDuration.value / curSongDuration.toFloat(),
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .graphicsLayer {
                        alpha = if (currentFraction > 0.001) 0f else 1f
                    }
            )
            PlayBarActionsMaximized(
                currentFraction,
                musicServiceConnection,
                title,
                onSkipNextPressed
            )
        }
    }
}

@Composable
fun PlayBarActionsMinimized(
    currentFraction: Float, musicServiceConnection: MusicServiceConnection,
    title: String, onSkipNextPressed: () -> Unit
) {
    Row(Modifier.graphicsLayer(alpha = 1f - currentFraction * 2)) {
        if (currentFraction != 1f) {
            Text(
                text = title,
                style = typography.h6.copy(fontSize = 10.sp),
                modifier = Modifier
                    .padding(8.dp)
                    .weight(1f),
            )
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                modifier = Modifier.padding(8.dp),
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
            IsLoading(musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_BUFFERING)
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
            Icon(
                painter = painterResource(id = R.drawable.exo_ic_skip_next),
                modifier = Modifier
                    .padding(8.dp)
                    .clickable(onClick = onSkipNextPressed),
                contentDescription = null,
            )
        }
    }
}

@Composable
fun PlayBarActionsMaximized(
    currentFraction: Float,
    musicServiceConnection: MusicServiceConnection,
    title: String,
    onSkipNextPressed: () -> Unit
) {
    if (currentFraction == 1f) {
        Row(Modifier.padding(top = heightSize(currentFraction).dp / 2)) {

            Icon(
                modifier = Modifier.size(40.dp),
                painter = painterResource(id = R.drawable.exo_styled_controls_shuffle_on),
                contentDescription = null,
            )
            Icon(
                modifier = Modifier.size(40.dp),
                painter = painterResource(id = R.drawable.exo_ic_skip_previous),
                contentDescription = null,
            )
            if (musicServiceConnection.playbackState.value?.state != PlaybackState.STATE_PLAYING &&
                musicServiceConnection.playbackState.value?.state != PlaybackState.STATE_BUFFERING
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.exo_icon_play),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(
                            onClick = { musicServiceConnection.transportControls.play() }
                        ),
                    contentDescription = null
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.exo_icon_pause),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(
                            onClick = { musicServiceConnection.transportControls.pause() }
                        ),
                    contentDescription = null
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.exo_ic_skip_next),
                modifier = Modifier
                    .size(40.dp)
                    .clickable(onClick = onSkipNextPressed),
                contentDescription = null,
            )
            Icon(
                modifier = Modifier.size(40.dp),
                painter = painterResource(id = R.drawable.exo_controls_repeat_all),
                contentDescription = null,
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
