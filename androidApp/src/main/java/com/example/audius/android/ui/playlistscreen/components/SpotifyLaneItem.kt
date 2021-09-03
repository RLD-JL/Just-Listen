package com.example.audius.android.ui.playlistscreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.example.audius.android.ui.test.Album
import com.example.audius.viewmodel.screens.trending.PlaylistItem

@Composable
fun SpotifyLaneItem(playlistItem: PlaylistItem) {
    val context = LocalContext.current
    Column(
        modifier =
        Modifier
            .width(180.dp)
            .padding(8.dp)
            .clickable(
                onClick = {
                    //Disclaimer: We should pass event top level and there should startActivity
                })
    ) {
        Image(
            painter = rememberImagePainter(playlistItem.songIconList.songImageURL480px),
            modifier = Modifier
                .width(180.dp)
                .height(160.dp),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Text(
            text = "${playlistItem.title}: by ${playlistItem.user}",
            style = typography.body2,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}