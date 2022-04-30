package com.rld.justlisten.android.ui.playlistscreen.components

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.rld.justlisten.android.ui.theme.typography
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

@Composable
fun PlaylistRowItem(
    playlistItem: PlaylistItem,
    onPlaylistClicked: (String, String, String, String) -> Unit) {
    Column(
        modifier =
        Modifier
            .width(180.dp)
            .padding(8.dp)
            .clickable(
                onClick = {
                })
    ) {
        val request = ImageRequest.Builder(context = LocalContext.current)
            .placeholder(ColorDrawable(MaterialTheme.colors.secondary.toArgb()))
            .data(playlistItem.songIconList.songImageURL480px).build()

        val painter = rememberImagePainter(request = request)
        Image(
            painter = painter,
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}
