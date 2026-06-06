package com.rld.justlisten.ui.bottombars.playbar.components.addplaylist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rld.justlisten.ui.components.CustomPlaceholderIcon
import com.rld.justlisten.ui.addplaylistscreen.components.AddPlaylistDialog
import com.rld.justlisten.ui.addplaylistscreen.components.PlaylistViewItem
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import kotlinx.coroutines.launch

@Composable
fun AddPlaylistOption(
    title: String,
    addPlaylistList: List<AddPlaylist>,
    onAddPlaylistClicked: (String, String?) -> Unit,
    clickedToAddSongToPlaylist: (String, String?, List<String>) -> Unit,
    currentSongId: String? = null,
) {
    val openDialog = remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (addPlaylistList.isEmpty()) {
            EmptyPlaylistsPlaceholder()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp)
            ) {
                itemsIndexed(items = addPlaylistList) { _, playlist ->
                    PlaylistViewItem(
                        playlist = playlist,
                        currentSongId = currentSongId,
                        clickedToAddSongToPlaylist = { playlistName, playlistDescription, updatedSongsList ->
                            // Add/remove in database
                            clickedToAddSongToPlaylist(playlistName, playlistDescription, updatedSongsList)
                            // Trigger Snackbar
                            coroutineScope.launch {
                                val isAdded = currentSongId != null && updatedSongsList.contains(currentSongId)
                                val message = if (isAdded) {
                                    "\"$title\" added to $playlistName"
                                } else {
                                    "\"$title\" removed from $playlistName"
                                }
                                snackbarHostState.currentSnackbarData?.dismiss()
                                val snackbarJob = launch {
                                    snackbarHostState.showSnackbar(
                                        message = message,
                                        withDismissAction = true,
                                        duration = SnackbarDuration.Indefinite
                                    )
                                }
                                kotlinx.coroutines.delay(1000L)
                                snackbarJob.cancel()
                                snackbarHostState.currentSnackbarData?.dismiss()
                            }
                        }
                    )
                }
            }
        }

        // Floating action button aligned to bottom-right corner
        FloatingActionButton(
            onClick = { openDialog.value = true },
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Playlist",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // SnackbarHost to display the "Song added/removed" messages
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )

        AddPlaylistDialog(
            openDialog = openDialog,
            onAddPlaylistClicked = onAddPlaylistClicked
        )
    }
}

@Composable
fun EmptyPlaylistsPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Glowing radial background holding the custom vector icon
        Box(
            modifier = Modifier.size(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.05f),
                                Color.Transparent
                            )
                        )
                    )
            )
            CustomPlaceholderIcon(
                modifier = Modifier.size(110.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Create Your First Playlist",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Save your favorite tracks and build custom collections. Tap the '+' button below to get started.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

