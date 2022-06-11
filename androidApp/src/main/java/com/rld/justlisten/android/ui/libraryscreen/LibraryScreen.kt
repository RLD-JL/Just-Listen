package com.rld.justlisten.android.ui.libraryscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rld.justlisten.android.exoplayer.MusicServiceConnection
import com.rld.justlisten.android.ui.libraryscreen.components.FavoritePlaylist
import com.rld.justlisten.android.ui.libraryscreen.components.MostPlayedSongs
import com.rld.justlisten.android.ui.libraryscreen.components.PlaylistView
import com.rld.justlisten.android.ui.libraryscreen.components.RowListOfRecentActivity
import com.rld.justlisten.android.ui.playlistdetailscreen.playMusicFromId
import com.rld.justlisten.android.ui.playlistscreen.Header
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.screens.library.LibraryState
import com.rld.justlisten.viewmodel.screens.search.TrackItem

@Composable
fun LibraryScreen(
    musicServiceConnection: MusicServiceConnection,
    libraryState: LibraryState,
    onFavoritePlaylistPressed: (String, String, String, String) -> Unit,
    onMostPlaylistPressed: (String, String, String, String) -> Unit,
    onPlayListViewClicked: () -> Unit,
    lasItemReached: (Int) -> Unit
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(5.dp)) {
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
                lasItemReached = lasItemReached,
                lastIndexReached = libraryState.lastIndexReached
            )
            Divider(thickness = 1.dp)
            PlaylistView(onPlayListViewClicked)
            Spacer(modifier = Modifier.height(10.dp))
            FavoritePlaylist(libraryState, onFavoritePlaylistPressed)
            Spacer(modifier = Modifier.height(10.dp))
            MostPlayedSongs(libraryState, onMostPlaylistPressed)
        }
    }
}