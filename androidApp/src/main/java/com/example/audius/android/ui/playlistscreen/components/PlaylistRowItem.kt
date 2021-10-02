package com.example.audius.android.ui.playlistscreen.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.audius.android.ui.theme.typography
import com.example.audius.viewmodel.screens.playlist.PlaylistItem

@Composable
fun PlaylistRowItem(
    playlistItem: PlaylistItem,
    onPlaylistClicked: (String, String, String, String) -> Unit
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
        Image(
            painter = rememberImagePainter(playlistItem.songIconList.songImageURL480px),
            modifier = Modifier
                .width(180.dp)
                .height(160.dp)
                .clickable(
                    onClick = {
                        onPlaylistClicked(
                            playlistItem.id,
                            playlistItem.songIconList.songImageURL480px,
                            playlistItem.user,
                            playlistItem.playlistTitle
                        )
                    }
                ),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Text(
            text = "${playlistItem.playlistTitle}: by ${playlistItem.user}",
            style = typography.subtitle2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
