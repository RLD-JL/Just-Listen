package com.example.audius.android.ui.playlistscreen.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.support.v4.media.MediaBrowserCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.exoplayer.utils.Constants
import com.example.audius.android.ui.test.Album
import com.example.audius.viewmodel.screens.trending.PlaylistItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun SpotifyLaneItem(
    playlistItem: PlaylistItem,
    musicServiceConnection: MusicServiceConnection,
    onPlaylistClicked: (String, String) -> Unit
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
                            playlistItem.songIconList.songImageURL480px
                        )
                        musicServiceConnection.subscribe(
                            Constants.CLICKED_PLAYLIST,
                            object : MediaBrowserCompat.SubscriptionCallback() {})
                    }
                ),
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
