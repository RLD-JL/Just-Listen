package com.example.audius.android.ui.bottombars.playbar.components

import android.media.session.PlaybackState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audius.android.R
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.bottombars.playbar.IsLoading
import com.example.audius.android.ui.theme.typography

@Composable
fun PlayBarActionsMinimized(
    currentFraction: Float, musicServiceConnection: MusicServiceConnection,
    title: String, onSkipNextPressed: () -> Unit
) {
    Row(
        Modifier
            .graphicsLayer(alpha = 1f - currentFraction * 2)
            .height(IntrinsicSize.Max)
    ) {
        if (currentFraction != 1f) {
            Text(
                text = title,
                style = typography.h6.copy(fontSize = 10.sp),
                modifier = Modifier
                    .padding(8.dp)
                    .weight(0.7f),
                maxLines = 3
            )

            if (musicServiceConnection.isFavorite.value)
            {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    tint = Color.Red,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(25.dp),
                    contentDescription = null
                )
            } else {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(25.dp),
                    contentDescription = null
                )
            }
            if (musicServiceConnection.playbackState.value?.state != PlaybackState.STATE_PLAYING &&
                musicServiceConnection.playbackState.value?.state != PlaybackState.STATE_BUFFERING
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.exo_icon_play),
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable(
                            onClick = { musicServiceConnection.transportControls.play() }
                        )
                        .size(30.dp),
                    contentDescription = null
                )
            }
            IsLoading(
                musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_BUFFERING,
                Modifier
            )
            if (musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PLAYING) {
                Icon(
                    painter = painterResource(id = R.drawable.exo_icon_pause),
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable(
                            onClick = { musicServiceConnection.transportControls.pause() }
                        )
                        .size(30.dp),
                    contentDescription = null
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.exo_ic_skip_next),
                modifier = Modifier
                    .padding(8.dp)
                    .clickable(onClick = onSkipNextPressed)
                    .size(30.dp),
                contentDescription = null,
            )
        }
    }
}