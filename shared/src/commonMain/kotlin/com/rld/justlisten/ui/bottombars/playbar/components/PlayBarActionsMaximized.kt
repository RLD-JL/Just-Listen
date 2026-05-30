package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.media.RepeatMode
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.models.SongIconList

@Composable
fun PlayBarActionsMaximized(
    bottomPadding: Dp,
    currentFraction: Float,
    musicPlayer: MusicPlayer,
    title: String,
    onSkipNextPressed: () -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    onSaveClicked: () -> Unit,
) {
    val playbackState by musicPlayer.playbackState.collectAsState()
    val artist = playbackState.currentMedia?.artist ?: ""

    if (currentFraction > 0.5f) {
        val sliderPosition = if (playbackState.currentMedia?.duration ?: 0L > 0) {
            playbackState.currentPosition.toFloat() / playbackState.currentMedia!!.duration.toFloat()
        } else 0f
        
        Column(
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    alpha = ((currentFraction - 0.5f) * 2f).coerceIn(0f, 1f)
                }
                .padding(horizontal = 24.dp)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Song Title and Artist
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artist,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(Modifier.height(24.dp))

            // Social Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SocialButton(icon = if (playbackState.currentMedia?.isFavorite == true) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder, text = null) {
                    playbackState.currentMedia?.let {
                        onFavoritePressed(it.id, it.title, UserModel(it.artist), SongIconList(it.artworkUrl ?: "", it.artworkUrl ?: "", it.artworkUrl ?: ""), !it.isFavorite)
                    }
                }
                SocialButton(icon = Icons.Default.Add, text = "Save") {
                    onSaveClicked()
                }
                SocialButton(icon = Icons.Outlined.Share, text = "Share")
            }

            Spacer(Modifier.height(24.dp))

            // Slider and Time Labels
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = sliderPosition,
                    onValueChange = {
                        val newPos = (it * (playbackState.currentMedia?.duration ?: 0L)).toLong()
                        musicPlayer.seekTo(newPos)
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    )
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatTime(playbackState.currentPosition), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    Text(text = formatTime(playbackState.currentMedia?.duration ?: 0L), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Playback Controls
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { musicPlayer.setShuffleModeEnabled(!playbackState.isShuffleModeEnabled) }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = null,
                        tint = if (playbackState.isShuffleModeEnabled) Color.Green else Color.White
                    )
                }
                
                IconButton(onClick = { musicPlayer.skipToPrevious() }, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.1f))
                        .clickable { if (playbackState.status == PlaybackStatus.PLAYING) musicPlayer.pause() else musicPlayer.play() },
                    contentAlignment = Alignment.Center
                ) {
                    if (playbackState.status == PlaybackStatus.BUFFERING) {
                        CircularProgressIndicator(modifier = Modifier.size(40.dp), color = Color.White)
                    } else {
                        Icon(
                            imageVector = if (playbackState.status == PlaybackStatus.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                IconButton(onClick = onSkipNextPressed, modifier = Modifier.size(48.dp)) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                }
                
                IconButton(onClick = { 
                    val nextMode = when(playbackState.repeatMode) {
                        RepeatMode.NONE -> RepeatMode.ONE
                        RepeatMode.ONE -> RepeatMode.ALL
                        RepeatMode.ALL -> RepeatMode.NONE
                    }
                    musicPlayer.setRepeatMode(nextMode)
                }) {
                    val icon = when (playbackState.repeatMode) {
                        RepeatMode.NONE -> Icons.Default.Repeat
                        RepeatMode.ONE -> Icons.Default.RepeatOne
                        RepeatMode.ALL -> Icons.Default.Repeat
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (playbackState.repeatMode != RepeatMode.NONE) Color.Green else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SocialButton(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String? = null, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        if (text != null) {
            Spacer(Modifier.width(8.dp))
            Text(text = text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes >= 60) {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        "${hours}:${remainingMinutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    } else {
        "${minutes}:${seconds.toString().padStart(2, '0')}"
    }
}
