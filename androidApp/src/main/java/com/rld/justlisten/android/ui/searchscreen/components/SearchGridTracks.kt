package com.rld.justlisten.android.ui.searchscreen.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.util.fastForEach
import com.rld.justlisten.android.ui.playlistscreen.components.TrackGridItem
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.viewmodel.interfaces.Item

@Composable
fun SearchGridTracks(list: List<Item>, onSongPressed: (String, String, String, SongIconList) -> Unit) {
    VerticalGrid {
        list.fastForEach { item ->
            TrackGridItem(item, onSongPressed)
        }
    }
}