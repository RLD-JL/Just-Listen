package com.rld.justlisten.ui.addplaylistscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.runtime.Composable
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

@Composable
fun PlaylistViewItem(
    playlist: AddPlaylist,
    currentSongId: String? = null,
    clickedToAddSongToPlaylist: (String, String?, List<String>) -> Unit,
) {
    val isAlreadyInPlaylist = currentSongId != null && playlist.songsList?.contains(currentSongId) == true

    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f))
            .clickable(onClick = {
                val updatedSongs = if (currentSongId != null) {
                    if (isAlreadyInPlaylist) {
                        playlist.songsList?.filter { it != currentSongId } ?: emptyList()
                    } else {
                        (playlist.songsList ?: emptyList()) + currentSongId
                    }
                } else {
                    playlist.songsList ?: emptyList()
                }
                clickedToAddSongToPlaylist(
                    playlist.playlistName,
                    playlist.playlistDescription,
                    updatedSongs
                )
            })
            .padding(14.dp)
    ) {
        val hasSongs = !playlist.songsList.isNullOrEmpty()
        
        // Premium default icon/artwork holder
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isAlreadyInPlaylist) {
                            listOf(Color(0xFFE91E63), Color(0xFF9C27B0)) // Pink/purple gradient when song is added
                        } else if (hasSongs) {
                            listOf(Color(0xFF3F51B5), Color(0xFF00BCD4)) // Cool blue/indigo gradient when populated
                        } else {
                            listOf(Color(0xFF888888), Color(0xFF555555)) // Muted gray gradient when empty
                        }
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            CustomPlaceholderIcon(
                modifier = Modifier.fillMaxSize(0.5f),
                noteColor = Color.White,
                lineColor = Color.White.copy(alpha = 0.8f),
                dotColor = Color.White.copy(alpha = 0.9f)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = playlist.playlistName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${playlist.songsList?.size ?: 0} songs",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }

        if (currentSongId != null) {
            Icon(
                imageVector = if (isAlreadyInPlaylist) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isAlreadyInPlaylist) "Remove from playlist" else "Add to playlist",
                tint = if (isAlreadyInPlaylist) Color(0xFFE91E63) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
