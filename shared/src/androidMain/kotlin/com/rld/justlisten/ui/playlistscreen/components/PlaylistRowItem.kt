package com.rld.justlisten.ui.playlistscreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import coil3.request.allowHardware
import com.rld.justlisten.ui.components.AnimatedShimmer
import com.rld.justlisten.ui.theme.typography
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

@Composable
fun PlaylistRowItem(
    playlistItem: PlaylistItem,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit
) {
    Column(
        modifier =
        Modifier
            .width(180.dp)
            .padding(8.dp)
            .clickable(
                onClick = {
                })
    ) {

        val context = LocalPlatformContext.current
        val painter = rememberAsyncImagePainter(
            model = remember(playlistItem.songIconList.songImageURL480px, context) {
                ImageRequest.Builder(context)
                    .data(playlistItem.songIconList.songImageURL480px)
                    .allowHardware(true)
                    .build()
            }
        )
        val state by painter.state.collectAsState()

        Box(
            modifier = Modifier
                .width(180.dp)
                .height(160.dp)
                .clickable(onClick = {
                    onPlaylistClicked(
                        playlistItem.id,
                        playlistItem.songIconList.songImageURL480px,
                        playlistItem.user,
                        playlistItem.playlistTitle,
                        playlistItem.isFavorite
                    )
                }
                )
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
        }

        Text(
            text = "${playlistItem.playlistTitle}: by ${playlistItem.user}",
            style = typography.titleSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
