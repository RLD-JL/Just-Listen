package com.rld.justlisten.ui.libraryscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.rld.justlisten.ui.components.CustomPlaceholderIcon
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.ui.addplaylistscreen.components.ConfirmDeletePlaylistDialog

@Composable
fun MyPlaylistRowItem(
    playlist: AddPlaylist,
    onPlaylistClicked: (String, String?, List<String>) -> Unit
) {
    val hasSongs = !playlist.songsList.isNullOrEmpty()

    Column(
        modifier = Modifier
            .width(150.dp)
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable {
                onPlaylistClicked(
                    playlist.playlistName,
                    playlist.playlistDescription,
                    playlist.songsList ?: emptyList()
                )
            }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(134.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = if (hasSongs) {
                            listOf(Color(0xFF3F51B5), Color(0xFF00BCD4)) // Cool indigo/cyan gradient when populated
                        } else {
                            listOf(Color(0xFF9C27B0), Color(0xFFE91E63)) // Cyber pink/purple gradient when empty
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            CustomPlaceholderIcon(
                modifier = Modifier.fillMaxSize(0.45f),
                noteColor = Color.White,
                lineColor = Color.White.copy(alpha = 0.8f),
                dotColor = Color.White.copy(alpha = 0.9f)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = playlist.playlistName,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Text(
            text = "${playlist.songsList?.size ?: 0} songs",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            maxLines = 1,
            modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 2.dp)
        )
    }
}

