package com.example.audius.android.ui.bottombars.playbar.components.addplaylist

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.example.audius.android.ui.addplaylistscreen.PlaylistViewItem
import com.example.audius.android.ui.bottombars.playbar.components.more.TopSection
import com.example.audius.datalayer.localdb.addplaylistscreen.AddPlaylist

@Composable
fun AddPlaylistOption(
    title: String,
    painter: MutableState<Painter?>,
    addPlaylistList: List<AddPlaylist>
) {
    Column(
        Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.75f)
    ) {
        TopSection(title, painter)
        Divider(color = MaterialTheme.colors.primary, thickness = 0.5.dp)
        addPlaylistList.fastForEach {
            PlaylistViewItem(it)
        }
    }
}

