package com.rld.justlisten.android.ui.bottombars.playbar.components.addplaylist

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.rld.justlisten.android.ui.addplaylistscreen.AddPlaylistDialog
import com.rld.justlisten.android.ui.addplaylistscreen.AddPlaylistRow
import com.rld.justlisten.android.ui.addplaylistscreen.PlaylistViewItem
import com.rld.justlisten.android.ui.bottombars.playbar.components.more.TopSection
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.AddPlaylist

@Composable
fun AddPlaylistOption(
    title: String,
    painter: MutableState<Painter?>,
    addPlaylistList: List<AddPlaylist>,
    onAddPlaylistClicked: (String, String?) -> Unit,
    clickedToAddSongToPlaylist: (String, String?, List<String>) -> Unit
) {
    val openDialog = remember { mutableStateOf(false) }

    LazyColumn {

        item {
            TopSection(title, painter)
            Divider(color = MaterialTheme.colors.primary, thickness = 0.5.dp)
        }

        itemsIndexed(items = addPlaylistList) { index, playlist ->
            PlaylistViewItem(playlist, clickedToAddSongToPlaylist)
        }

        item {
            AddPlaylistRow(openDialog)
            AddPlaylistDialog(openDialog, onAddPlaylistClicked)
        }
    }
}
