package com.example.audius.android.ui.bottombars

import android.media.session.PlaybackState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.audius.android.R
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.theme.graySurface
import com.example.audius.android.ui.theme.typography
import com.example.audius.android.ui.theme.utils.ThemeMode

@Composable
fun PlayerBottomBar(
    modifier: Modifier,
    songIcon: String,
    title: String,
    onSkipNextPressed: () -> Unit,
    musicServiceConnection: MusicServiceConnection
) {
    val backgroundColor =
        if (isSystemInDarkTheme()) graySurface else MaterialTheme.colors.background
    val bottomBarHeight = 57.dp
    Row(
        modifier = modifier
            .padding(bottom = bottomBarHeight)
            .fillMaxWidth()
            .background(color = backgroundColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberImagePainter(songIcon),
            modifier = Modifier.size(65.dp),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        Text(
            text = title,
            style = typography.h6.copy(fontSize = 14.sp),
            modifier = Modifier
                .padding(8.dp)
                .weight(1f),
        )
        Icon(
            imageVector = Icons.Default.FavoriteBorder, modifier = Modifier.padding(8.dp),
            contentDescription = null
        )
        if (musicServiceConnection.playbackState.value?.state != PlaybackState.STATE_PLAYING &&
            musicServiceConnection.playbackState.value?.state != PlaybackState.STATE_BUFFERING) {
            Icon(
                painter = painterResource(id = R.drawable.exo_icon_play), modifier = Modifier
                    .padding(8.dp)
                    .clickable(
                        onClick = { musicServiceConnection.transportControls.play() }
                    ),
                contentDescription = null
            )
        }
        if (musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PLAYING)
        {
            Icon(
                painter = painterResource(id = R.drawable.exo_icon_pause), modifier = Modifier
                    .padding(8.dp)
                    .clickable(
                        onClick = { musicServiceConnection.transportControls.pause() }
                    ),
                contentDescription = null
            )
        }
            isLoading(musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_BUFFERING)

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
fun isLoading(isLoading: Boolean) {
    if (isLoading) {

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(24.dp*2f)
         ) {
            CircularProgressIndicator()
            Icon(
                painter = painterResource(id = R.drawable.exo_icon_pause), modifier = Modifier,
                contentDescription = null
            )
        }
    }
}
