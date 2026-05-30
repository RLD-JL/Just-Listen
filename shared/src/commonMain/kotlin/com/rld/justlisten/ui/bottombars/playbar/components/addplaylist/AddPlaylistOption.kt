package com.rld.justlisten.ui.bottombars.playbar.components.addplaylist

import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.rld.justlisten.ui.addplaylistscreen.components.AddPlaylistDialog
import com.rld.justlisten.ui.addplaylistscreen.components.PlaylistViewItem
import com.rld.justlisten.ui.bottombars.playbar.components.more.TopSection
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist

@Composable
fun AddPlaylistOption(
    title: String,
    painter: MutableState<Painter?>,
    addPlaylistList: List<AddPlaylist>,
    onAddPlaylistClicked: (String, String?) -> Unit,
    clickedToAddSongToPlaylist: (String, String?, List<String>) -> Unit,
) {
    val openDialog = remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
        ) {
            item {
                TopSection(title, painter)
                Divider(thickness = 2.dp)
            }
            itemsIndexed(items = addPlaylistList) { _, playlist ->
                PlaylistViewItem(playlist, clickedToAddSongToPlaylist)
            }
        }

        ExtendedFloatingActionButton(
            icon = { Icon(Icons.Default.Add, contentDescription = null, tint = Color.White) },
            text = { Text("New Playlist", color = Color.White, fontWeight = FontWeight.Bold) },
            onClick = { openDialog.value = true },
            backgroundColor = MaterialTheme.colors.primaryVariant,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp)
        )

        AddPlaylistDialog(openDialog, onAddPlaylistClicked)
    }
}
