package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.media.RepeatMode
import com.rld.justlisten.ui.utils.offsetX
import com.rld.justlisten.ui.utils.widthSize

@Composable
fun PlayBarActionsMaximized(
    bottomPadding: Dp,
    currentFraction: Float,
    musicPlayer: MusicPlayer,
    title: String,
    onSkipNextPressed: () -> Unit,
    maxWidth: Float,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val playbackState by musicPlayer.playbackState.collectAsState()

    if (currentFraction > 0.001f) {
        val sliderPosition = if (playbackState.currentMedia?.duration ?: 0L > 0) {
            playbackState.currentPosition.toFloat() / playbackState.currentMedia!!.duration.toFloat()
        } else 0f
        
        Column(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    alpha = ((currentFraction - 0.5f) * 2f).coerceIn(0f, 1f)
                }
                .padding(bottom = bottomPadding + 5.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = title,
                textAlign = TextAlign.Center
            )
            
            Slider(
                interactionSource = interactionSource,
                modifier = Modifier
                    .offset(x = offsetX(currentFraction, maxWidth).dp)
                    .width(widthSize(currentFraction, maxWidth).dp),
                value = sliderPosition, 
                onValueChange = {
                    val newPos = (it * (playbackState.currentMedia?.duration ?: 0L)).toLong()
                    musicPlayer.seekTo(newPos)
                })

            Row(
                Modifier.height(IntrinsicSize.Max),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.weight(0.2f),
                    onClick = { musicPlayer.setShuffleModeEnabled(!playbackState.isShuffleModeEnabled) }
                ) {
                    Icon(
                        imageVector = if (playbackState.isShuffleModeEnabled) Icons.Default.ShuffleOn else Icons.Default.Shuffle,
                        contentDescription = null,
                    )
                }
                
                IconButton(
                    modifier = Modifier.weight(0.2f),
                    onClick = { musicPlayer.skipToPrevious() }
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = null,
                    )
                }

                if (playbackState.status != PlaybackStatus.PLAYING &&
                    playbackState.status != PlaybackStatus.BUFFERING
                ) {
                    OutlinedButton(
                        shape = CircleShape,
                        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onSurface),
                        modifier = Modifier.size(48.dp).weight(0.2f),
                        onClick = { musicPlayer.play() }) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                    }
                } else {
                    OutlinedButton(
                        shape = CircleShape,
                        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onSurface),
                        modifier = Modifier.size(48.dp).weight(0.2f),
                        onClick = { musicPlayer.pause() }) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = null
                        )
                    }
                }

                IconButton(
                    modifier = Modifier.weight(0.2f),
                    onClick = onSkipNextPressed
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = null,
                    )
                }
                
                IconButton(
                    modifier = Modifier.weight(0.2f),
                    onClick = { 
                        val nextMode = when(playbackState.repeatMode) {
                            RepeatMode.NONE -> RepeatMode.ONE
                            RepeatMode.ONE -> RepeatMode.ALL
                            RepeatMode.ALL -> RepeatMode.NONE
                        }
                        musicPlayer.setRepeatMode(nextMode)
                    }
                ) {
                    val icon = when (playbackState.repeatMode) {
                        RepeatMode.NONE -> Icons.Default.Repeat
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                        RepeatMode.ALL -> Icons.Default.RepeatOn // Or custom
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}
