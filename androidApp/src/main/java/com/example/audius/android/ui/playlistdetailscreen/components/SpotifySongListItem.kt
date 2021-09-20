package com.example.audius.android.ui.playlistdetailscreen.components

import android.support.v4.media.MediaBrowserCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.exoplayer.utils.Constants.CLICKED_PLAYLIST
import com.example.audius.android.ui.test.Album
import com.example.audius.viewmodel.screens.trending.PlaylistItem

@Composable
fun SpotifySongListItem(playlistItem: PlaylistItem, musicServiceConnection: MusicServiceConnection, playlist: List<PlaylistItem>) {
    Row(
        modifier = Modifier.padding(8.dp).clickable(
            onClick = {musicServiceConnection.updatePlaylist(playlist)
                        musicServiceConnection.subscribe(CLICKED_PLAYLIST, object : MediaBrowserCompat.SubscriptionCallback() {})
                musicServiceConnection.transportControls.playFromMediaId(playlistItem.id, null)}
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = rememberImagePainter(playlistItem.songIconList.songImageURL150px),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(55.dp)
                .padding(4.dp)
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .weight(1f)
        ) {
            Text(
                text = playlistItem.playlistTitle,
                style = typography.h6.copy(fontSize = 16.sp),
                color = MaterialTheme.colors.onSurface
            )
            Text(
                text = "${playlistItem.title} by ${playlistItem.user}",
                style = typography.subtitle2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = MaterialTheme.colors.primaryVariant,
                modifier = Modifier
                    .padding(4.dp)
                    .size(20.dp)
            )
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.padding(4.dp)
        )
    }
}
