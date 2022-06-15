package com.rld.justlisten.android.ui.addplaylistscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rld.justlisten.android.R
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.AddPlaylist

@Composable
fun PlaylistViewItem(
    playlist: AddPlaylist,
    clickedToAddSongToPlaylist: (String, String?, List<String>) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(onClick = {
                clickedToAddSongToPlaylist(
                    playlist.playlistName,
                    playlist.playlistDescription,
                    playlist.songsList ?: emptyList()
                )
            })
    ) {
        Icon(
            painterResource(id = R.drawable.ic_playlist_icon),
            contentDescription = null,
            modifier = Modifier.height(75.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(playlist.playlistName, modifier = Modifier.fillMaxWidth())
    }
}