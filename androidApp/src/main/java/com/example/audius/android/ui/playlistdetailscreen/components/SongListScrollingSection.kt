package com.example.audius.android.ui.playlistdetailscreen.components

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
import com.example.audius.datalayer.models.SongIconList
import com.example.audius.datalayer.models.UserModel
import com.example.audius.viewmodel.screens.playlist.PlaylistItem

@Composable
fun SongListScrollingSection(
    playlist: List<PlaylistItem>,
    onSongClicked: (String, String, UserModel, SongIconList) -> Unit,
    onShuffleClicked: () -> Unit,
    dominantColor: (Int) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList) -> Unit
) {
    ShuffleButton(onShuffleClicked)
    DownloadedRow()
    playlist.forEach { playlistItem ->
        SongListItem(
            playlistItem = playlistItem,
            onSongClicked = onSongClicked,
            onFavoritePressed = onFavoritePressed,
            dominantColor = dominantColor
        )
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
fun ShuffleButton(onShuffleClicked: () -> Unit) {
    Button(
        onClick = { onShuffleClicked() },
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
