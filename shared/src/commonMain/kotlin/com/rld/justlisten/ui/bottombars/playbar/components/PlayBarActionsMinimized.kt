package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel

@Composable
fun PlayBarActionsMinimized(
    currentFraction: Float,
    musicPlayer: MusicPlayer,
    title: String,
    onSkipNextPressed: () -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    songIcon: String,
    playBarMinimizedClicked: () -> Unit
) {
    val playbackState by musicPlayer.playbackState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(65.dp)
            .graphicsLayer {
                alpha = 1f - (currentFraction * 2f).coerceIn(0f, 1f)
            }
            .clickable(onClick = playBarMinimizedClicked),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(65.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.h6.copy(fontSize = 14.sp),
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        IconButton(onClick = {
             // Handle favorite
        }) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = MaterialTheme.colors.onBackground
            )
        }
        IconButton(onClick = {
            if (playbackState.status == PlaybackStatus.PLAYING) musicPlayer.pause() else musicPlayer.play()
        }) {
            if (playbackState.status == PlaybackStatus.BUFFERING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colors.onBackground,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = if (playbackState.status == PlaybackStatus.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = MaterialTheme.colors.onBackground
                )
            }
        }
        IconButton(onClick = onSkipNextPressed) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = null,
                tint = MaterialTheme.colors.onBackground
            )
        }
    }
}
