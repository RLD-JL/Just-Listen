package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import com.rld.justlisten.ui.components.MusicLoadingSpinner
import androidx.compose.material3.MaterialTheme
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
import com.rld.justlisten.ui.LocalMusicPlayer

@Composable
fun PlayBarActionsMinimized(
    currentFraction: Float,
    onSkipNextPressed: () -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit
) {
    val musicPlayer = LocalMusicPlayer.current
    val playbackState by musicPlayer.playbackState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Exactly 65dp — same as minibar height in JustListenScaffold
            .height(65.dp)
            .graphicsLayer {
                // Fade out fast so it's gone before the image reaches mid-expansion
                alpha = (1f - currentFraction * 3f).coerceIn(0f, 1f)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.weight(1f))

        IconButton(onClick = {
            playbackState.currentMedia?.let { media ->
                onFavoritePressed(
                    media.id,
                    media.title,
                    UserModel(media.artist),
                    SongIconList(
                        media.artworkUrl ?: "",
                        media.artworkUrl ?: "",
                        media.artworkUrl ?: ""
                    ),
                    !media.isFavorite
                )
            }
        }) {
            Icon(
                imageVector = if (playbackState.currentMedia?.isFavorite == true)
                    Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (playbackState.currentMedia?.isFavorite == true)
                    Color.Red else Color.White
            )
        }

        IconButton(onClick = {
            if (playbackState.status == PlaybackStatus.PLAYING)
                musicPlayer.pause()
            else
                musicPlayer.play()
        }) {
            if (playbackState.status == PlaybackStatus.BUFFERING) {
                MusicLoadingSpinner(
                    size = 20.dp,
                    color = Color.White
                )
            } else {
                Icon(
                    imageVector = if (playbackState.status == PlaybackStatus.PLAYING)
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