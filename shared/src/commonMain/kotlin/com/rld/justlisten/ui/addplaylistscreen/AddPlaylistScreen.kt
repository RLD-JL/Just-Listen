package com.rld.justlisten.ui.addplaylistscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rld.justlisten.ui.addplaylistscreen.components.AddPlaylistDialog
import com.rld.justlisten.ui.addplaylistscreen.components.AddPlaylistRow
import com.rld.justlisten.ui.addplaylistscreen.components.PlaylistViewItem
import com.rld.justlisten.viewmodel.screens.addplaylist.AddPlaylistState

import com.rld.justlisten.ui.actions.AddPlaylistAction

@Composable
fun AddPlaylistScreen(
    addPlaylistState: AddPlaylistState,
    onAction: (AddPlaylistAction) -> Unit
) {
    val openDialog = remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        IconButton(modifier = Modifier.size(48.dp), onClick = { onAction(AddPlaylistAction.BackPressed(true)) }) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
            )
        }
        LazyColumn(Modifier.fillMaxWidth()) {
            item { AddPlaylistRow(openDialog) }
            item { HorizontalDivider(thickness = 2.dp) }
            item { 
                AddPlaylistDialog(
                    openDialog = openDialog,
                    onAddPlaylistClicked = { title, desc -> 
                        onAction(AddPlaylistAction.AddPlaylistClicked(title, desc)) 
                    }
                ) 
            }
            itemsIndexed(addPlaylistState.playlistsCreated) { _, playlist ->
                PlaylistViewItem(playlist) { title, desc, songs ->
                    onAction(AddPlaylistAction.AddSongToPlaylist(title, desc, songs))
                }
            }
        }
    }
}
