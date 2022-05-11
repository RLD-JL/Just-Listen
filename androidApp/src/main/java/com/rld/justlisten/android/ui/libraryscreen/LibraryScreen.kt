package com.rld.justlisten.android.ui.libraryscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.android.R
import com.rld.justlisten.android.exoplayer.MusicServiceConnection
import com.rld.justlisten.android.ui.playlistdetailscreen.playMusicFromId
import com.rld.justlisten.android.ui.playlistscreen.Header
import com.rld.justlisten.android.ui.playlistscreen.components.PlaylistRowItem
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.screens.library.LibraryState
import com.rld.justlisten.viewmodel.screens.search.TrackItem

@Composable
fun LibraryScreen(
    musicServiceConnection: MusicServiceConnection,
    libraryState: LibraryState,
    onPlaylistPressed: (String, String, String, String) -> Unit,
    onPlayListViewClicked: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(5.dp)) {
        Column {
            Header(text = "Last Played")
            RowListOfRecentActivity(
                libraryState,
                onPlaylistClicked = { id, songIcon, user, playlistTitle, isFavorite ->
                    run {
                        val playlistModel = PlayListModel(
                            id,
                            playlistTitle,
                            playlistTitle,
                            SongIconList(songIcon, songIcon, songIcon),
                            UserModel(user),
                            false
                        )
                        val item = TrackItem(playlistModel, isFavorite)
                        playMusicFromId(musicServiceConnection, listOf(item), id, false)
                    }
                },
            )
            Divider(thickness = 1.dp)
            PlaylistView(onPlayListViewClicked)
            Spacer(modifier = Modifier.height(10.dp))
            FavoritePlaylist(libraryState, onPlaylistPressed)
        }
    }
}

@Composable
fun RowListOfRecentActivity(
    libraryState: LibraryState,
    onPlaylistClicked: (String, String, String, String, Boolean) -> Unit,
) {

    LazyRow {
        itemsIndexed(items = libraryState.recentSongsItems) { _, playlistItem ->
            PlaylistRowItem(
                playlistItem = playlistItem,
                onPlaylistClicked = onPlaylistClicked,
            )
        }
    }
}

@Composable
fun PlaylistView(onPlayListViewClicked: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 15.dp)
            .clickable(onClick = onPlayListViewClicked),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = painterResource(id = R.drawable.ic_playlist), contentDescription = null)
        Text(
            modifier = Modifier.padding(start = 5.dp),
            text = "Playlists",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun FavoritePlaylist(
    libraryState: LibraryState,
    onPlaylistPressed: (String, String, String, String) -> Unit
) {
    val songIcon =
        if (libraryState.favoritePlaylistItems.isNotEmpty())
            libraryState.favoritePlaylistItems[0].songIconList.songImageURL480px
        else
            ""
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 15.dp)
            .clickable(
                onClick = {
                    onPlaylistPressed(
                        "Favorite",
                        songIcon,
                        "Favorite",
                        "You"
                    )
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = null)
        Text(
            modifier = Modifier.padding(start = 5.dp),
            text = "Favorite Playlist",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp
        )
    }
}