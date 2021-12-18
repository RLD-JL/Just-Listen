package com.example.audius.android.ui.bottombars.playbar

import android.media.session.PlaybackState
import androidx.compose.foundation.Image
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
import com.example.audius.android.ui.theme.modifiers.verticalGradientBackground
import com.example.audius.android.ui.theme.typography
import com.example.audius.android.ui.utils.*
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
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

@InternalCoroutinesApi
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
                    .height(IntrinsicSize.Max)
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
                onSkipNextPressed,
                constraints.maxWidth.value
            )
        }
    }
}

@Composable
fun PlayBarActionsMinimized(
    currentFraction: Float, musicServiceConnection: MusicServiceConnection,
    title: String, onSkipNextPressed: () -> Unit
) {
    Row(Modifier.graphicsLayer(alpha = 1f - currentFraction * 2).height(IntrinsicSize.Max)) {
        if (currentFraction != 1f) {
            Text(
                text = title,
                style = typography.h6.copy(fontSize = 10.sp),
                modifier = Modifier
                    .padding(8.dp)
                    .weight(0.7f),
                maxLines = 3
            )
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                modifier = Modifier.padding(8.dp).size(25.dp),
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
                        ).size(30.dp),
                    contentDescription = null
                )
            }
            IsLoading(musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_BUFFERING, Modifier)
            if (musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PLAYING) {
                Icon(
                    painter = painterResource(id = R.drawable.exo_icon_pause),
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable(
                            onClick = { musicServiceConnection.transportControls.pause() }
                        ).size(30.dp),
                    contentDescription = null
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.exo_ic_skip_next),
                modifier = Modifier
                    .padding(8.dp)
                    .clickable(onClick = onSkipNextPressed).size(30.dp),
                contentDescription = null,
            )
        }
    }
}

@Composable
fun IsLoading(isLoading: Boolean, modifier: Modifier) {
    if (isLoading) {
        Box(modifier =  modifier.size(46.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
            Icon(
                painter = painterResource(id = R.drawable.exo_icon_pause), modifier = modifier.size(30.dp),
                contentDescription = null
            )
        }
    }
}
