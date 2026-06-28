package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.ui.text.TextStyle
import com.rld.justlisten.ui.components.SmartMarqueeText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.unit.DpSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.media.RepeatMode
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.viewmodel.player.PlayerUiState
import com.rld.justlisten.ui.actions.PlayerAction
import com.rld.justlisten.ui.bottombars.playbar.PlayerLayoutInfo
import com.rld.justlisten.ui.bottombars.playbar.PlayerUiEvent
import com.rld.justlisten.ui.components.MusicLoadingSpinner
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import justlisten.shared.generated.resources.Res
import justlisten.shared.generated.resources.ic_repost
import com.rld.justlisten.ui.seeallscreen.formatCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayBarActionsMaximized(
    uiState: PlayerUiState,
    layoutInfo: PlayerLayoutInfo,
    onAction: (PlayerAction) -> Unit,
    onUiEvent: (PlayerUiEvent) -> Unit
) {
    val musicPlayer = LocalMusicPlayer.current
    val playbackState = uiState.playbackState ?: com.rld.justlisten.media.PlaybackState(
        status = com.rld.justlisten.media.PlaybackStatus.IDLE,
        currentPosition = 0
    )
    val artist = playbackState.currentMedia?.artist ?: ""
    val title = playbackState.currentMedia?.title ?: ""

    val bottomPadding = layoutInfo.bottomPadding
    val isVisible by remember(layoutInfo) {
        derivedStateOf {
            layoutInfo.currentFractionProvider() > 0.5f
        }
    }

    if (isVisible) {
        Column(
            Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    val fraction = layoutInfo.currentFractionProvider()
                    alpha = ((fraction - 0.5f) * 2f).coerceIn(0f, 1f)
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
                SmartMarqueeText(
                    text = title,
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                val artistId = playbackState.currentMedia?.artistId ?: ""
                val artistColor = if (artistId.isNotBlank()) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f)
                val artistClickAction = if (artistId.isNotBlank()) {
                    { onUiEvent(PlayerUiEvent.NavigateToArtist(artistId, artist)) }
                } else null

                SmartMarqueeText(
                    text = artist,
                    color = artistColor,
                    style = TextStyle(
                        fontSize = 16.sp
                    ),
                    onClick = artistClickAction
                )
            }
            
            Spacer(Modifier.height(16.dp))

            // Social Buttons Row
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    val favoriteCountText = formatCount(playbackState.currentMedia?.favoriteCount ?: 0)
                    SocialButton(
                        icon = if (playbackState.currentMedia?.isFavorite == true) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        text = favoriteCountText
                    ) {
                        playbackState.currentMedia?.let {
                            onAction(
                                 PlayerAction.ToggleFavorite(
                                     songId = it.id,
                                     title = it.title,
                                     user = UserModel(username = it.artist, id = it.artistId),
                                     songIcon = SongIconList(it.artworkUrl ?: "", it.artworkUrl ?: "", it.artworkUrl ?: ""),
                                     isFavorite = !it.isFavorite
                                 )
                            )
                        }
                    }
                }
                item {
                    val isReposted = playbackState.currentMedia?.isReposted == true
                    val repostCountText = formatCount(playbackState.currentMedia?.repostCount ?: 0)
                    SocialButton(
                        painter = painterResource(Res.drawable.ic_repost),
                        text = repostCountText,
                        tint = if (isReposted) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f)
                    ) {
                        playbackState.currentMedia?.let {
                            onAction(PlayerAction.ToggleRepost(it.id, !isReposted))
                        }
                    }
                }
                item {
                    val commentCountText = formatCount(playbackState.currentMedia?.commentCount ?: 0)
                    SocialButton(
                        icon = Icons.AutoMirrored.Outlined.Comment,
                        text = commentCountText
                    ) {
                        playbackState.currentMedia?.let {
                            onUiEvent(PlayerUiEvent.OpenComments(it.id))
                        }
                    }
                }
                item {
                    SocialButton(icon = Icons.Default.Add, text = "Save") {
                        onAction(PlayerAction.LoadPlaylists)
                        onUiEvent(PlayerUiEvent.OpenAddPlaylist)
                    }
                }
                item {
                    SocialButton(icon = Icons.Outlined.Share, text = "Share")
                }
            }

            Spacer(Modifier.height(16.dp))

            // Slider and Time Labels
            PlaybackSeekBar(
                musicPlayer = musicPlayer
            )

            Spacer(Modifier.height(12.dp))

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
                        tint = if (playbackState.isShuffleModeEnabled) MaterialTheme.colorScheme.primary else Color.White
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
                        MusicLoadingSpinner(size = 32.dp, color = Color.White)
                    } else {
                        Icon(
                            imageVector = if (playbackState.status == PlaybackStatus.PLAYING) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                IconButton(onClick = { onAction(PlayerAction.SkipNext) }, modifier = Modifier.size(48.dp)) {
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
                        tint = if (playbackState.repeatMode != RepeatMode.NONE) MaterialTheme.colorScheme.primary else Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun SocialButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String? = null,
    tint: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        if (text != null) {
            Spacer(Modifier.width(8.dp))
            Text(text = text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun SocialButton(
    painter: androidx.compose.ui.graphics.painter.Painter,
    text: String? = null,
    tint: Color = Color.White,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painter, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaybackSeekBar(
    musicPlayer: MusicPlayer
) {
    val playbackState by musicPlayer.playbackState.collectAsState()
    val duration = playbackState.currentMedia?.duration ?: 0L
    val coroutineScope = rememberCoroutineScope()
    var dragPosition by remember { mutableStateOf<Float?>(null) }
    var seekJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(playbackState.currentMedia?.id) {
        seekJob?.cancel()
        dragPosition = null
    }

    val sliderPosition = if (duration > 0L) {
        playbackState.currentPosition.toFloat() / duration.toFloat()
    } else 0f

    val displayPosition = dragPosition ?: sliderPosition
    val displayPositionMs = if (dragPosition != null) {
        (dragPosition!! * duration).toLong()
    } else {
        playbackState.currentPosition
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            Slider(
                value = displayPosition,
                onValueChange = {
                    seekJob?.cancel()
                    dragPosition = it
                },
                onValueChangeFinished = {
                    dragPosition?.let { pos ->
                        val newPos = (pos * duration).toLong()
                        musicPlayer.seekTo(newPos)
                        seekJob = coroutineScope.launch {
                            delay(500)
                            dragPosition = null
                        }
                    }
                },
                thumb = {
                    SliderDefaults.Thumb(
                        interactionSource = remember { MutableInteractionSource() },
                        colors = SliderDefaults.colors(thumbColor = Color.White),
                        thumbSize = DpSize(12.dp, 12.dp)
                    )
                },
                colors = SliderDefaults.colors(
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = formatTime(displayPositionMs), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
            Text(text = formatTime(duration), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        }
    }
}
