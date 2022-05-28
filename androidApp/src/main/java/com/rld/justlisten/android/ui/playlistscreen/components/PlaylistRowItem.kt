package com.rld.justlisten.android.ui.playlistscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.rld.justlisten.android.ui.components.AnimatedShimmer
import com.rld.justlisten.android.ui.theme.typography
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

        SubcomposeAsyncImage(model = ImageRequest.Builder(LocalContext.current)
            .data(playlistItem.songIconList.songImageURL480px)
            .crossfade(true)
            .build(),
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
                ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            loading = {
                AnimatedShimmer(180.dp, 160.dp)
            }
        )

        Text(
            text = "${playlistItem.playlistTitle}: by ${playlistItem.user}",
            style = typography.subtitle2,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
