package com.rld.justlisten.android.ui.addplaylistscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rld.justlisten.android.ui.addplaylistscreen.components.AddPlaylistDialog
import com.rld.justlisten.android.ui.addplaylistscreen.components.AddPlaylistRow
import com.rld.justlisten.android.ui.addplaylistscreen.components.PlaylistViewItem
import com.rld.justlisten.viewmodel.Events
import com.rld.justlisten.viewmodel.screens.addplaylist.AddPlaylistState

@Composable
fun AddPlaylistScreen(
    addPlaylistState: AddPlaylistState,
    onAddPlaylistClicked: (String, String?) -> Unit,
    clickedToAddSongToPlaylist: (String, String?, List<String>) -> Unit,
    events: Events,
    onBackButtonPressed: (Boolean) -> Unit
) {
    val openDialog = remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth()) {
        IconButton(modifier = Modifier.size(48.dp), onClick = {onBackButtonPressed(true)  }) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = null,
            )
        }
        LazyColumn(Modifier.fillMaxWidth()) {
            item { AddPlaylistRow(openDialog) }
            item { Divider(thickness = 2.dp) }
            item { AddPlaylistDialog(openDialog, onAddPlaylistClicked) }
            itemsIndexed(addPlaylistState.playlistsCreated) { _, playlist ->
                PlaylistViewItem(playlist, clickedToAddSongToPlaylist, events)
            }
        }
    }
}

