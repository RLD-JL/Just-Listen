package com.rld.justlisten.ui.playlistdetailscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.SwitchDefaults
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
    onRepostPressed: (String, Boolean) -> Unit,
    painter: AsyncImagePainter,
    onArtistClicked: (String, String) -> Unit,
    onDeleteSong: (String) -> Unit,
    currentPlayingSongId: String? = null
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
        itemsIndexed(playlist, key = { _, item -> item.id }) { index, playlistItem ->
            val isUserPlaylist = playlistDetailState.playlistEnum == "CREATED_BY_USER"
            val isPlaying = playlistItem.id == currentPlayingSongId
            SongListItem(
                playlist = playlistDetailState.playlistName,
                playlistItem = playlistItem,
                onSongClicked = onSongClicked,
                onFavoritePressed = onFavoritePressed,
                onRepostPressed = onRepostPressed,
                onArtistClicked = onArtistClicked,
                canDelete = isUserPlaylist,
                onDelete = { onDeleteSong(playlistItem.id) },
                isPlaying = isPlaying,
                showShareButton = false
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
                onCheckedChange = { switched = it },
                enabled = false,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFFE91E63),
                    disabledCheckedThumbColor = Color.White.copy(alpha = 0.5f),
                    disabledCheckedTrackColor = Color(0xFFE91E63).copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
fun ShuffleButton(onShuffleClicked: () -> Unit) {
    Button(
        onClick = { onShuffleClicked() },
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp),
        shape = CircleShape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 48.dp)
            .height(48.dp)
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFE91E63), Color(0xFF9C27B0))
                ),
                shape = CircleShape
            ),
    ) {
        Text(
            text = "SHUFFLE PLAY",
            style = typography.titleMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        )
    }
}
