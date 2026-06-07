package com.rld.justlisten.ui.playlistdetailscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.rld.justlisten.ui.theme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.rld.justlisten.ui.components.ConfirmDialog
import com.rld.justlisten.ui.components.MusicLoadingSpinner
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.ui.seeallscreen.formatCount
import org.jetbrains.compose.resources.painterResource
import justlisten.shared.generated.resources.Res
import justlisten.shared.generated.resources.ic_repost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SongListItem(
    playlistItem: PlaylistItem,
    onSongClicked: (String) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    onRepostPressed: (String, Boolean) -> Unit,
    playlist: String,
    onArtistClicked: (String, String) -> Unit,
    isPlaying: Boolean = false,
    canDelete: Boolean = false,
    onDelete: () -> Unit = {}
) {
    if (canDelete) {
        val showConfirmDialog = remember { mutableStateOf(false) }

        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                dismissValue == SwipeToDismissBoxValue.EndToStart
            }
        )

        LaunchedEffect(dismissState.settledValue) {
            if (dismissState.settledValue == SwipeToDismissBoxValue.EndToStart) {
                showConfirmDialog.value = true
            }
        }

        LaunchedEffect(showConfirmDialog.value) {
            if (!showConfirmDialog.value && dismissState.settledValue == SwipeToDismissBoxValue.EndToStart) {
                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
            }
        }

        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                val color = when (dismissState.dismissDirection) {
                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFE91E63)
                    else -> Color.Transparent
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            content = {
                SongListItemContent(
                    playlistItem = playlistItem,
                    onSongClicked = onSongClicked,
                    onFavoritePressed = onFavoritePressed,
                    onRepostPressed = onRepostPressed,
                    playlist = playlist,
                    onArtistClicked = onArtistClicked,
                    isPlaying = isPlaying
                )
            }
        )

        ConfirmDialog(
            title = "Delete Song",
            description = "Are you sure you want to delete \"${playlistItem.title.ifBlank { playlistItem.playlistTitle }}\" from this playlist?",
            confirmText = "Yes, Delete",
            cancelText = "Cancel",
            openDialog = showConfirmDialog,
            onConfirm = onDelete
        )
    } else {
        SongListItemContent(
            playlistItem = playlistItem,
            onSongClicked = onSongClicked,
            onFavoritePressed = onFavoritePressed,
            onRepostPressed = onRepostPressed,
            playlist = playlist,
            onArtistClicked = onArtistClicked,
            isPlaying = isPlaying
        )
    }
}

@Composable
private fun SongListItemContent(
    playlistItem: PlaylistItem,
    onSongClicked: (String) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    onRepostPressed: (String, Boolean) -> Unit,
    playlist: String,
    onArtistClicked: (String, String) -> Unit,
    isPlaying: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
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
            if (isPlaying) {
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
                text = playlistItem.title.ifBlank { playlistItem.playlistTitle },
                style = typography.titleMedium.copy(fontSize = 16.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val artistId = playlistItem._data.user.id
                val artistColor = if (artistId.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                val artistClickAction = if (artistId.isNotBlank()) {
                    { onArtistClicked(artistId, playlistItem.user) }
                } else null

                Text(
                    text = "by ${playlistItem.user}",
                    style = typography.titleSmall,
                    color = artistColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .then(
                            if (artistClickAction != null) {
                                Modifier.clickable(onClick = artistClickAction)
                            } else {
                                Modifier
                            }
                        )
                )
                Text(
                    text = " • ${formatDuration(playlistItem.duration)}",
                    style = typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
                val playsText = if (playlistItem.playCount == 1) "1 play" else "${formatCount(playlistItem.playCount)} plays"
                Text(
                    text = " • $playsText",
                    style = typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
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
                            playlistItem._data.user, playlistItem.songIconList,
                            !isFavorite
                        )
                    }
            )

        Spacer(modifier = Modifier.width(8.dp))

        val isReposted = playlistItem.isReposted
        Icon(
            painter = painterResource(Res.drawable.ic_repost),
            contentDescription = "Repost",
            tint = if (isReposted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .padding(4.dp)
                .size(20.dp)
                .clickable {
                    onRepostPressed(playlistItem.id, !isReposted)
                }
        )
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "$m:${s.toString().padStart(2, '0')}"
}

