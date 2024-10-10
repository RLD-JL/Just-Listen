package com.rld.justlisten.android.ui.addplaylistscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.RoundedCornersTransformation
import com.rld.justlisten.android.R
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.viewmodel.Events
import com.rld.justlisten.viewmodel.screens.library.getSongWithId

@Composable
fun PlaylistViewItem(
    playlist: AddPlaylist,
    clickedToAddSongToPlaylist: (String, String?, List<String>) -> Unit,
    events: Events?
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(10.dp)
            .clickable(onClick = {
                clickedToAddSongToPlaylist(
                    playlist.playlistName,
                    playlist.playlistDescription,
                    playlist.songsList ?: emptyList()
                )
            })
    ) {
        val playListModel: PlayListModel?
        if (!playlist.songsList.isNullOrEmpty()) {
            playListModel = events?.getSongWithId(playlist.songsList!![0])
            AsyncImage(
                modifier = Modifier.height(50.dp).width(50.dp),
                model = ImageRequest.Builder(LocalContext.current)
                    .data( playListModel?.songImgList?.songImageURL480px).transformations(RoundedCornersTransformation(10f))
                    .build(),
                contentDescription = "Icon"
            )
        } else {
            Icon(
                painterResource(id = R.drawable.ic_playlist_icon),
                contentDescription = null,
                modifier = Modifier.height(75.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))
        Text(playlist.playlistName, modifier = Modifier.fillMaxWidth())
    }
}