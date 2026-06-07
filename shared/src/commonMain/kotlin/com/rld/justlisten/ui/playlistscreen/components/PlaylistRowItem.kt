package com.rld.justlisten.ui.playlistscreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.ui.components.AnimatedShimmer
import com.rld.justlisten.ui.components.MusicLoadingSpinner
import com.rld.justlisten.ui.theme.typography
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

@Composable
fun PlaylistRowItem(
    playlistItem: PlaylistItem,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit,
    onArtistClicked: ((String, String) -> Unit)? = null,
    isPlaying: Boolean = false
) {


    Column(
        modifier = Modifier
            .width(180.dp)
            .padding(8.dp)
    ) {
        val context = LocalPlatformContext.current
        val painter = rememberAsyncImagePainter(
            model = remember(playlistItem.songIconList.songImageURL480px, context) {
                ImageRequest.Builder(context)
                    .data(playlistItem.songIconList.songImageURL480px)
                    .build()
            }
        )
        val state by painter.state.collectAsState()

        Box(
            modifier = Modifier
                .width(180.dp)
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable(onClick = {
                    onPlaylistClicked(
                        playlistItem.id,
                        playlistItem.songIconList.songImageURL480px,
                        playlistItem.user,
                        playlistItem.playlistTitle,
                        playlistItem.isFavorite
                    )
                })
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (state is AsyncImagePainter.State.Loading) {
                AnimatedShimmer(180.dp, 160.dp)
            }
            
            if (isPlaying) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    MusicLoadingSpinner(
                        size = 20.dp,
                        color = Color.White
                    )
                }
            }
        }

        Text(
            text = playlistItem.playlistTitle,
            style = typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 8.dp)
        )
        val artistId = playlistItem._data.user.id
        Text(
            text = "by ${playlistItem.user}",
            style = typography.bodySmall.copy(
                color = if (onArtistClicked != null && artistId.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .padding(top = 2.dp)
                .then(
                    if (onArtistClicked != null && artistId.isNotBlank()) {
                        Modifier.clickable { onArtistClicked(artistId, playlistItem.user) }
                    } else Modifier
                )
        )
    }
}
