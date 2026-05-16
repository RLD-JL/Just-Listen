package com.rld.justlisten.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.datacalls.addplaylistscreen.getAddPlaylist
import com.rld.justlisten.datalayer.datacalls.addplaylistscreen.savePlaylist
import com.rld.justlisten.datalayer.datacalls.addplaylistscreen.updatePlaylistSongs
import com.rld.justlisten.datalayer.datacalls.library.saveSongToFavorites
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.navigation.AppNavigation
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.ui.bottombars.bottombarnav.Level1BottomBar
import com.rld.justlisten.ui.bottombars.playbar.PlayerBarSheetContent
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun JustListenScaffold(
    navController: NavHostController,
    musicPlayer: MusicPlayer,
    showDonationTab: Boolean,
    modifier: Modifier = Modifier,
    repository: Repository,
) {
    val playbackState by musicPlayer.playbackState.collectAsState()
    val shouldShowPlayBar = playbackState.status == PlaybackStatus.PLAYING ||
        playbackState.status == PlaybackStatus.PAUSED ||
        playbackState.status == PlaybackStatus.BUFFERING ||
        playbackState.currentMedia != null

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val routeLabel = navBackStackEntry?.destination?.route.orEmpty()
    val showBottomBar = !routeLabel.contains("PlaylistDetail") && !routeLabel.contains("AddPlaylist")

    val addPlaylistList = remember { mutableStateOf(emptyList<AddPlaylist>()) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                Level1BottomBar(
                    navController = navController,
                    showDonationTab = showDonationTab,
                )
            }
        },
    ) { innerPadding ->
        Box(Modifier.fillMaxSize().padding(innerPadding)) {
            Column(Modifier.fillMaxSize()) {
                Box(
                    Modifier
                        .weight(1f)
                        .padding(bottom = if (shouldShowPlayBar) 65.dp else 0.dp),
                ) {
                    AppNavigation(navController = navController)
                }
            }
            if (shouldShowPlayBar) {
                PlayerBarSheetContent(
                    bottomPadding = 0.dp,
                    currentFraction = 0f,
                    isExtended = false,
                    onSkipNextPressed = { musicPlayer.skipToNext() },
                    musicPlayer = musicPlayer,
                    onCollapsedClicked = {},
                    onFavoritePressed = { id, title, user, songIcon, isFavorite ->
                        repository.saveSongToFavorites(
                            id, title, user, songIcon, "Favorite", isFavorite = isFavorite,
                        )
                    },
                    addPlaylistList = addPlaylistList.value,
                    onAddPlaylistClicked = { name, description ->
                        repository.savePlaylist(name, description)
                        addPlaylistList.value = repository.getAddPlaylist()
                    },
                    getLatestPlaylist = {
                        addPlaylistList.value = repository.getAddPlaylist()
                    },
                    clickedToAddSongToPlaylist = { title, description, songs ->
                        repository.updatePlaylistSongs(title, description, songs)
                    },
                    newDominantColor = {},
                    playBarMinimizedClicked = {},
                )
            }
        }
    }
}
