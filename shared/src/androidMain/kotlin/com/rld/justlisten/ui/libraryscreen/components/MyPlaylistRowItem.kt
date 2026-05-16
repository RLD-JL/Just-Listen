package com.rld.justlisten.ui.libraryscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import org.jetbrains.compose.resources.painterResource
import justlisten.shared.generated.resources.Res
import justlisten.shared.generated.resources.ic_playlist_icon

@Composable
fun MyPlaylistRowItem(
    playlist: AddPlaylist,
    onPlaylistClicked: (String, String?, List<String>) -> Unit,
    onDeleteClicked: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .width(150.dp)
            .padding(8.dp)
            .clickable {
                onPlaylistClicked(
                    playlist.playlistName,
                    playlist.playlistDescription,
                    playlist.songsList ?: emptyList()
                )
            }
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Icon(
                painterResource(Res.drawable.ic_playlist_icon),
                contentDescription = null,
                modifier = Modifier
                    .size(134.dp)
                    .padding(8.dp)
            )
            IconButton(
                onClick = { onDeleteClicked(playlist.playlistName) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Playlist",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
        Text(
            text = playlist.playlistName,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = "${playlist.songsList?.size ?: 0} songs",
            fontSize = 12.sp,
            color = Color.Gray,
            maxLines = 1
        )
    }
}
