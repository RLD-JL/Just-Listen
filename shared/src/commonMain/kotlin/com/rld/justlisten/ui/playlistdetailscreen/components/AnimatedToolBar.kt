package com.rld.justlisten.ui.playlistdetailscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.rld.justlisten.navigation.Route
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rld.justlisten.ui.theme.modifiers.horizontalGradientBackground
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState

@Composable
fun AnimatedToolBar(
    playlistDetailState: PlaylistDetailState,
    scrollState: MutableState<Float>,
    onBackButtonPressed: (Boolean) -> Unit,
    onDeletePlaylistClicked: (String) -> Unit,
    onEditPlaylistClicked: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalGradientBackground(
                if (Dp(-scrollState.value) < 1080.dp)
                    listOf(
                        Color.Transparent,
                        Color.Transparent
                    ) else listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        IconButton(modifier = Modifier.size(48.dp), onClick = { onBackButtonPressed(true) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
            )
        }
        Text(
            text = playlistDetailState.playlistName,
            modifier = Modifier
                .alpha(((-scrollState.value + 0.010f) / 1000).coerceIn(0f, 1f))
        )
        val clipboardManager = LocalClipboardManager.current
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "Playlist Options",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Share playlist") },
                    onClick = {
                        showMenu = false
                        val imageUrl = if (playlistDetailState.playlistIcon.isNotBlank()) {
                            playlistDetailState.playlistIcon
                        } else {
                            playlistDetailState.songPlaylist.firstOrNull()?.songIconList?.songImageURL480px.orEmpty()
                        }
                        val playlistDetail = Route.PlaylistDetail(
                            playlistId = playlistDetailState.playlistId,
                            playlistIcon = imageUrl,
                            playlistTitle = playlistDetailState.playlistName,
                            playlistCreatedBy = playlistDetailState.playListCreatedBy,
                            playlistEnum = playlistDetailState.playlistEnum,
                            songsList = playlistDetailState.songPlaylist.map { it.id }
                        )
                        val base64Data = com.rld.justlisten.util.PlaylistShareUtils.exportPlaylist(playlistDetail)
                        val url = "justlisten://playlist/import?data=$base64Data"
                        clipboardManager.setText(AnnotatedString(url))
                        com.rld.justlisten.ui.utils.showToast("Playlist share link copied!")
                    },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                )
                if (playlistDetailState.playListCreatedBy == "ME" || playlistDetailState.playListCreatedBy == "ME (Audius)") {
                    DropdownMenuItem(
                        text = { Text("Edit details") },
                        onClick = {
                            showMenu = false
                            onEditPlaylistClicked()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Delete playlist",
                                color = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            showMenu = false
                            onDeletePlaylistClicked(playlistDetailState.playlistName)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}


