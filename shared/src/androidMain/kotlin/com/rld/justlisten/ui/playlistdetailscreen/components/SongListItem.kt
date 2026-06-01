package com.rld.justlisten.ui.playlistdetailscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import com.rld.justlisten.ui.theme.typography
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.rld.justlisten.ui.components.AnimatedShimmer
import com.rld.justlisten.ui.components.MusicLoadingSpinner
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

@Composable
fun SongListItem(
    playlistItem: PlaylistItem, onSongClicked: (String) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    playlist: String
) {
    val musicPlayer = LocalMusicPlayer.current
    val playbackState by musicPlayer.playbackState.collectAsState()
    val isPlayingThisSong = playbackState.status == PlaybackStatus.PLAYING &&
            playbackState.currentMedia?.id == playlistItem.id

    Row(
        modifier = Modifier
            .padding(8.dp)
            .clickable(
                onClick = {
                    onSongClicked(playlistItem.id)
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val context = LocalPlatformContext.current
        val painter = rememberAsyncImagePainter(
            model = remember(playlistItem.songIconList.songImageURL150px, context) {
                ImageRequest.Builder(context)
                    .data(playlistItem.songIconList.songImageURL150px)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
            }
        )
        val state by painter.state.collectAsState()

        Box(
            modifier = Modifier
                .size(55.dp)
                .padding(4.dp)
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (state is AsyncImagePainter.State.Loading) {
                AnimatedShimmer(width = 55.dp, height = 55.dp)
            }
            if (isPlayingThisSong) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    MusicLoadingSpinner(
                        size = 18.dp,
                        color = Color.White
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .weight(1f)
        ) {
            Text(
                text = playlistItem.playlistTitle,
                style = typography.titleMedium.copy(fontSize = 16.sp),
            )
            Text(
                text = "${playlistItem.title} by ${playlistItem.user}",
                style = typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (playlist == "Most Played") {
            Text(text = playlistItem.songCounter)
        }

        val isFavorite = playlistItem.isFavorite
        Icon(imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier
                    .padding(4.dp)
                    .size(20.dp)
                    .clickable {
                        onFavoritePressed(
                            playlistItem.id, playlistItem.title,
                            UserModel(playlistItem.user), playlistItem.songIconList,
                            !isFavorite
                        )
                    }
            )
    }
}

