package com.rld.justlisten.ui.playlistdetailscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import com.rld.justlisten.ui.theme.typography
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImagePainter
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState

@Composable
fun SongListScrollingSection(
    playlistDetailState: PlaylistDetailState,
    scrollState: MutableState<Float>,
    playlist: List<PlaylistItem>,
    onSongClicked: (String) -> Unit,
    onShuffleClicked: () -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    painter: AsyncImagePainter
) {
    LazyColumn(Modifier.padding(top = 25.dp)) {
        item {
            BoxTopSection(
                scrollState = scrollState,
                playlistDetailState = playlistDetailState,
                playlistPainter = painter
            )

        }
        item {
            ShuffleButton(onShuffleClicked)
            DownloadedRow()
        }
        itemsIndexed(playlist) { index, playlistItem ->

            SongListItem(
                playlist = playlistDetailState.playlistName,
                playlistItem = playlistItem,
                onSongClicked = onSongClicked,
                onFavoritePressed = onFavoritePressed,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
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
            style = typography.titleMedium.copy(fontSize = 14.sp)
        )
        var switched by remember { mutableStateOf(true) }
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            Switch(
                checked = switched,
                modifier = Modifier.padding(8.dp),
                onCheckedChange = { switched = it }
            )
        }
    }
}

@Composable
fun ShuffleButton(onShuffleClicked: () -> Unit) {
    Button(
        onClick = { onShuffleClicked() },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 100.dp)
            .clip(CircleShape),
    ) {
        Text(
            text = "SHUFFLE PLAY",
            style = typography.titleMedium.copy(fontSize = 14.sp),
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
        )
    }
}
