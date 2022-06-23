package com.rld.justlisten.android.ui.searchscreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rld.justlisten.android.ui.playlistscreen.components.Header
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.screens.search.TrackItem

@Composable
fun ShowSearchResults(
    searchResultTracks: List<TrackItem>,
    searchResultPlaylist: List<PlaylistItem>,
    onSongPressed: (String, String, String, SongIconList) -> Unit,
    onPlaylistPressed: (String, String, String, String, Boolean) -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Header(text = "Top Find")
        SearchGridTracks(list = searchResultTracks, onSongPressed)
        Header(text = "Playlist", modifier = Modifier.padding(top = 10.dp))
        PlaylistResult(playlist = searchResultPlaylist, onPlaylistPressed)
    }
}