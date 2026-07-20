package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import com.rld.justlisten.ui.components.MusicLoadingSpinner
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.rld.justlisten.media.PlaybackStatus

@Composable
fun PlayBarActionsMinimized(
    modifier: Modifier = Modifier,
    currentFractionProvider: () -> Float,
    status: PlaybackStatus,
    onPlayPause: () -> Unit,
    onSkipNextPressed: () -> Unit
) {
    Row(
        modifier = modifier
            // Exactly 65dp — same as minibar height in JustListenScaffold
            .height(65.dp)
            .graphicsLayer {
                val currentFraction = currentFractionProvider()
                // Fade out fast so it's gone before the image reaches mid-expansion
                alpha = (1f - currentFraction * 3f).coerceIn(0f, 1f)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPlayPause) {
            if (status == PlaybackStatus.BUFFERING) {
                MusicLoadingSpinner(
                    size = 20.dp,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = if (status == PlaybackStatus.PLAYING)
                        Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White
                )
            }
        }

        IconButton(onClick = onSkipNextPressed) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}
