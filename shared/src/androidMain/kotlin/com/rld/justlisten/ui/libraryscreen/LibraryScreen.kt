package com.rld.justlisten.ui.libraryscreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.libraryscreen.components.FavoritePlaylist
import com.rld.justlisten.ui.libraryscreen.components.MostPlayedSongs
import com.rld.justlisten.ui.libraryscreen.components.PlaylistView
import com.rld.justlisten.ui.libraryscreen.components.RowListOfRecentActivity
import com.rld.justlisten.ui.libraryscreen.components.MyPlaylistRowItem
import com.rld.justlisten.ui.playlistscreen.components.Header
import com.rld.justlisten.ui.utils.playMusicFromId
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.screens.library.LibraryState
import com.rld.justlisten.viewmodel.screens.search.TrackItem

@Composable
fun LibraryScreen(
    musicPlayer: MusicPlayer,
    libraryState: LibraryState,
    onFavoritePlaylistPressed: (String, String, String, String) -> Unit,
    onMostPlaylistPressed: (String, String, String, String) -> Unit,
    onPlayListViewClicked: () -> Unit,
    onPlaylistCreatedClicked: (String, String?, List<String>) -> Unit,
    onDeletePlaylistClicked: (String) -> Unit,
    lasItemReached: (Int) -> Unit,
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(5.dp)) {
        val scrollState = rememberScrollState()
        Column(Modifier.verticalScroll(scrollState)) {
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
                        playMusicFromId(musicPlayer, listOf(item), id)
                    }
                },
                lasItemReached = lasItemReached,
                lastIndexReached = libraryState.lastIndexReached
            )
            Divider(thickness = 1.dp)
            PlaylistView(onPlayListViewClicked)
            
            if (libraryState.playlistsCreated.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Header(text = "My Playlists")
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(libraryState.playlistsCreated) { playlist ->
                         MyPlaylistRowItem(
                             playlist, 
                             onPlaylistClicked = onPlaylistCreatedClicked,
                             onDeleteClicked = onDeletePlaylistClicked
                         )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            FavoritePlaylist(libraryState, onFavoritePlaylistPressed)
            Spacer(modifier = Modifier.height(10.dp))
            MostPlayedSongs(libraryState, onMostPlaylistPressed)
        }
    }
}
