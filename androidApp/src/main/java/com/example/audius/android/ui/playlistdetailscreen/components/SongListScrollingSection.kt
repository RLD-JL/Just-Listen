package com.example.audius.android.ui.playlistdetailscreen.components

import android.media.browse.MediaBrowser
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.exoplayer.utils.Constants
import com.example.audius.android.exoplayer.utils.Constants.CLICKED_PLAYLIST
import com.example.audius.android.ui.bottombars.play
import com.example.audius.android.ui.test.AlbumsDataProvider
import com.example.audius.viewmodel.screens.trending.PlaylistItem
import com.google.android.exoplayer2.MediaMetadata


@Composable
fun SongListScrollingSection(playlist: List<PlaylistItem>, musicServiceConnection: MusicServiceConnection) {
    ShuffleButton(musicServiceConnection, playlist)
    DownloadedRow()
    playlist.forEach { playlistItem ->
        SpotifySongListItem(playlistItem = playlistItem, musicServiceConnection = musicServiceConnection, playlist = playlist)
    }

}

@Composable
fun DownloadedRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Download",
            style = typography.h6.copy(fontSize = 14.sp),
            color = MaterialTheme.colors.onSurface
        )
        var switched by remember { mutableStateOf(true) }
        Switch(
            checked = switched,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colors.primary
            ),
            modifier = Modifier.padding(8.dp),
            onCheckedChange = { switched = it }
        )
    }
}

@Composable
fun ShuffleButton(musicServiceConnection: MusicServiceConnection, playlist: List<PlaylistItem>) {
    Button(
        onClick = { musicServiceConnection.updatePlaylist(playlist)
            musicServiceConnection.subscribe(CLICKED_PLAYLIST, object: MediaBrowserCompat.SubscriptionCallback() {}) },
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 100.dp)
            .clip(CircleShape),
    ) {
        Text(
            text = "SHUFFLE PLAY",
            style = typography.h6.copy(fontSize = 14.sp),
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        )
    }
}
