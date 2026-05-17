package com.rld.justlisten.navigation

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.ui.addplaylistscreen.AddPlaylistScreen
import com.rld.justlisten.ui.donationscreen.DonationScreen
import com.rld.justlisten.ui.libraryscreen.LibraryScreen
import com.rld.justlisten.ui.playlistdetailscreen.PlaylistDetailScreen
import com.rld.justlisten.ui.playlistscreen.PlaylistScreen
import com.rld.justlisten.ui.searchscreen.SearchScreen
import com.rld.justlisten.ui.settingsscreen.SettingsScreen
import com.rld.justlisten.ui.utils.playMusicFromId
import com.rld.justlisten.viewmodel.addplaylist.AddPlaylistViewModel
import com.rld.justlisten.viewmodel.library.LibraryViewModel
import com.rld.justlisten.viewmodel.playlist.PlaylistViewModel
import com.rld.justlisten.viewmodel.playlistdetail.PlaylistDetailViewModel
import com.rld.justlisten.viewmodel.search.SearchViewModel
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import com.rld.justlisten.viewmodel.screens.settings.SettingsState
import org.koin.androidx.compose.koinViewModel

@Composable
actual fun LibraryScreenHost(navController: NavHostController) {
    val viewModel: LibraryViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val state by viewModel.libraryState.collectAsState()

    CollectNavigationEvents(viewModel, navController)

    LibraryScreen(
        musicPlayer = musicPlayer,
        libraryState = state,
        onFavoritePlaylistPressed = viewModel::onFavoritePlaylistClicked,
        onMostPlaylistPressed = viewModel::onMostPlayedPlaylistClicked,
        onPlayListViewClicked = viewModel::onAddPlaylistClicked,
        onPlaylistCreatedClicked = viewModel::onPlaylistCreatedClicked,
        onDeletePlaylistClicked = viewModel::deletePlaylist,
        lasItemReached = viewModel::loadMoreRecentSongs,
    )
}

@Composable
actual fun PlaylistScreenHost(navController: NavHostController) {
    val viewModel: PlaylistViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val state by viewModel.playlistState.collectAsState()

    CollectNavigationEvents(viewModel, navController)

    PlaylistScreen(
        playlistState = state,
        onPlaylistClicked = { id, icon, createdBy, title, _ ->
            viewModel.onPlaylistClicked(id, icon, createdBy, title)
        },
        onSearchClicked = viewModel::onSearchClicked,
        refreshScreen = viewModel::refreshScreen,
        onSongPressed = { songId, _, _, _ ->
            playMusicFromId(musicPlayer, state.tracksList, songId)
        },
        fetchPlaylist = viewModel::fetchPlaylist,
        getNewTracks = viewModel::getNewTracks,
    )
}

@Composable
actual fun PlaylistDetailScreenHost(
    navController: NavHostController,
    args: Route.PlaylistDetail,
) {
    val viewModel: PlaylistDetailViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val state by viewModel.playlistDetailState.collectAsState()

    LaunchedEffect(args) { viewModel.load(args) }
    CollectNavigationEvents(viewModel, navController)

    PlaylistDetailScreen(
        playlistDetailState = state,
        onBackButtonPressed = { if (it) viewModel.popBack() },
        musicPlayer = musicPlayer,
        onSongPressed = { songId ->
            playMusicFromId(musicPlayer, state.songPlaylist, songId)
        },
        onFavoritePressed = viewModel::onFavoritePressed,
        onDeletePlaylistClicked = viewModel::deletePlaylist,
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun SearchScreenHost(navController: NavHostController) {
    val viewModel: SearchViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val state by viewModel.searchState.collectAsState()

    CollectNavigationEvents(viewModel, navController)

    SearchScreen(
        onBackPressed = { viewModel.popBack() },
        onSearchPressed = viewModel::onSearchSubmitted,
        onSongPressed = { songId, _, _, _ ->
            playMusicFromId(musicPlayer, state.searchResultTracks, songId)
        },
        onPlaylistPressed = { id, icon, title, createdBy, _ ->
            viewModel.onPlaylistPressed(id, icon, title, createdBy)
        },
        searchScreenState = state,
    )
}

@Composable
actual fun AddPlaylistScreenHost(
    navController: NavHostController,
    args: Route.AddPlaylist,
) {
    val viewModel: AddPlaylistViewModel = koinViewModel()
    val state by viewModel.addPlaylistState.collectAsState()

    CollectNavigationEvents(viewModel, navController)

    AddPlaylistScreen(
        addPlaylistState = state,
        onAddPlaylistClicked = viewModel::onAddPlaylistClicked,
        clickedToAddSongToPlaylist = { title, description, songs ->
            viewModel.onPlaylistItemClicked(title, description, songs)
        },
        onBackButtonPressed = { if (it) viewModel.popBack() },
    )
}

@Composable
actual fun SettingsScreenHost(navController: NavHostController) {
    val viewModel: SettingsViewModel = koinViewModel()
    val state by viewModel.settingsState.collectAsState()

    CollectNavigationEvents(viewModel, navController)

    SettingsScreen(
        settings = SettingsState(
            isLoading = state.isLoading,
            hasDonationNavigationOn = state.hasDonationNavigationOn,
            isDarkThemeOn = state.isDarkThemeOn,
            palletColor = state.palletColor,
        ),
        updateSettings = { updated ->
            if (updated.isDarkThemeOn != state.isDarkThemeOn) {
                viewModel.onDarkModeToggled(updated.isDarkThemeOn)
            }
            if (updated.hasDonationNavigationOn != state.hasDonationNavigationOn) {
                viewModel.onDonationToggled(updated.hasDonationNavigationOn)
            }
            if (updated.palletColor != state.palletColor) {
                viewModel.onPaletteSelected(updated.palletColor)
            }
        },
    )
}

@Composable
actual fun DonationScreenHost(navController: NavHostController) {
    DonationScreen()
}
