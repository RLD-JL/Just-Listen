package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import com.rld.justlisten.ui.components.MusicLoadingSpinner
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
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
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel

@Composable
fun PlayBarActionsMinimized(
    currentFractionProvider: () -> Float,
    status: PlaybackStatus,
    isFavorite: Boolean,
    songId: String?,
    songTitle: String?,
    songArtist: String?,
    songArtistId: String?,
    songArtworkUrl: String?,
    onPlayPause: () -> Unit,
    onSkipNextPressed: () -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Exactly 65dp — same as minibar height in JustListenScaffold
            .height(65.dp)
            .graphicsLayer {
                val currentFraction = currentFractionProvider()
                // Fade out fast so it's gone before the image reaches mid-expansion
                alpha = (1f - currentFraction * 3f).coerceIn(0f, 1f)
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.weight(1f))

        IconButton(onClick = {
            if (songId != null) {
                onFavoritePressed(
                    songId,
                    songTitle ?: "",
                    UserModel(username = songArtist ?: "", id = songArtistId ?: ""),
                    SongIconList(
                        songArtworkUrl ?: "",
                        songArtworkUrl ?: "",
                        songArtworkUrl ?: ""
                    ),
                    !isFavorite
                )
            }
        }) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = null,
                tint = if (isFavorite) Color.Red else Color.White
            )
        }

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