@file:OptIn(androidx.compose.ui.ExperimentalComposeUiApi::class)
package com.rld.justlisten.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.ui.libraryscreen.LibraryScreen
import com.rld.justlisten.ui.playlistscreen.PlaylistScreen
import com.rld.justlisten.ui.playlistdetailscreen.PlaylistDetailScreen
import com.rld.justlisten.ui.searchscreen.SearchScreen
import com.rld.justlisten.ui.settingsscreen.SettingsScreen
import com.rld.justlisten.ui.donationscreen.DonationScreen
import com.rld.justlisten.ui.seeallscreen.SeeAllScreen
import com.rld.justlisten.ui.libraryscreen.MusicInsightsScreen
import com.rld.justlisten.ui.settingsscreen.CustomThemeScreen
import com.rld.justlisten.ui.addplaylistscreen.AddPlaylistScreen
import com.rld.justlisten.viewmodel.library.LibraryViewModel
import com.rld.justlisten.viewmodel.playlist.PlaylistViewModel
import com.rld.justlisten.viewmodel.playlistdetail.PlaylistDetailViewModel
import com.rld.justlisten.viewmodel.search.SearchViewModel
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import com.rld.justlisten.viewmodel.seeall.SeeAllViewModel
import com.rld.justlisten.viewmodel.addplaylist.AddPlaylistViewModel
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.ui.utils.playMusicFromId
import com.rld.justlisten.ui.actions.*
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import com.rld.justlisten.ui.JustListenApp

// Export the UIViewController wrapper for Xcode
fun MainViewController(): UIViewController = ComposeUIViewController {
    JustListenApp()
}

@Composable
actual fun LibraryScreenHost(navController: NavHostController) {
    val viewModel: LibraryViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val state by viewModel.libraryState.collectAsState()
    CollectNavigationEvents(viewModel, navController)
    LibraryScreen(
        musicPlayer = musicPlayer,
        libraryState = state,
        onAction = { action ->
            when (action) {
                is LibraryScreenAction.FavoritePlaylistPressed -> viewModel.onFavoritePlaylistClicked(action.playlistId, action.playlistIcon, action.playlistTitle, action.playlistCreatedBy)
                is LibraryScreenAction.MostPlayedPlaylistPressed -> viewModel.onMostPlayedPlaylistClicked(action.playlistId, action.playlistIcon, action.playlistTitle, action.playlistCreatedBy)
                LibraryScreenAction.PlayListViewClicked -> viewModel.onAddPlaylistClicked()
                is LibraryScreenAction.PlaylistCreatedClicked -> viewModel.onPlaylistCreatedClicked(action.title, action.description, action.songs)
                is LibraryScreenAction.DeletePlaylistClicked -> viewModel.deletePlaylist(action.playlistName)
                is LibraryScreenAction.LastItemReached -> viewModel.loadMoreRecentSongs(action.index)
                LibraryScreenAction.TimeCapsulePressed -> viewModel.onTimeCapsuleClicked()
                LibraryScreenAction.ExploreMusicPressed -> viewModel.onExploreMusicClicked()
                LibraryScreenAction.MusicInsightsPressed -> viewModel.onMusicInsightsClicked()
            }
        }
    )
}

@Composable
actual fun PlaylistScreenHost(navController: NavHostController) {
    val viewModel: PlaylistViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val repository: LibraryRepository = koinInject()
    val state by viewModel.playlistState.collectAsState()
    CollectNavigationEvents(viewModel, navController)
    PlaylistScreen(
        playlistState = state,
        onAction = { action ->
            when (action) {
                is PlaylistScreenAction.PlaylistClicked -> viewModel.onPlaylistClicked(action.playlistId, action.playlistIcon, action.createdBy, action.title)
                is PlaylistScreenAction.SongPressed -> playMusicFromId(musicPlayer, state.tracksList, action.songId, repository)
                PlaylistScreenAction.SearchClicked -> viewModel.onSearchClicked()
                PlaylistScreenAction.RefreshScreen -> viewModel.refreshScreen()
                is PlaylistScreenAction.FetchMorePlaylists -> viewModel.fetchPlaylist(action.index, action.category, action.query)
                is PlaylistScreenAction.ChangeTracksCategory -> viewModel.getNewTracks(action.category, action.timeRange)
                is PlaylistScreenAction.SeeAllClicked -> viewModel.onSeeAllClicked(action.categoryName, action.playlistEnum, action.queryPlaylist)
                is PlaylistScreenAction.SeeAllTracksClicked -> viewModel.onSeeAllTracksClicked(action.categoryName, action.queryPlaylist, action.selectedTimeRange)
            }
        }
    )
}

@Composable
actual fun PlaylistDetailScreenHost(navController: NavHostController, args: Route.PlaylistDetail) {
    val viewModel: PlaylistDetailViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val repository: LibraryRepository = koinInject()
    val state by viewModel.playlistDetailState.collectAsState()
    LaunchedEffect(args) { viewModel.load(args) }
    CollectNavigationEvents(viewModel, navController)
    PlaylistDetailScreen(
        playlistDetailState = state,
        musicPlayer = musicPlayer,
        onAction = { action ->
            when (action) {
                is PlaylistDetailAction.BackPressed -> if (action.isFromBottomSheet) viewModel.popBack()
                is PlaylistDetailAction.SongPressed -> playMusicFromId(musicPlayer, state.songPlaylist, action.songId, repository, state.playlistId)
                is PlaylistDetailAction.FavoritePressed -> {
                    viewModel.onFavoritePressed(action.songId, action.title, action.user, action.songIcon, action.isFavorite)
                    musicPlayer.refreshMetadata()
                }
                is PlaylistDetailAction.DeletePlaylistClicked -> viewModel.deletePlaylist(action.playlistName)
                is PlaylistDetailAction.EditPlaylistTitleClicked -> viewModel.editPlaylistTitle(action.oldName, action.newName)
            }
        }
    )
}

@Composable
actual fun SearchScreenHost(navController: NavHostController) {
    val viewModel: SearchViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val repository: LibraryRepository = koinInject()
    val state by viewModel.searchState.collectAsState()
    CollectNavigationEvents(viewModel, navController)
    SearchScreen(
        searchScreenState = state,
        onAction = { action ->
            when (action) {
                is SearchScreenAction.BackPressed -> viewModel.popBack()
                is SearchScreenAction.SearchPressed -> viewModel.onSearchSubmitted(action.query)
                is SearchScreenAction.QueryChanged -> viewModel.onSearchQueryChanged(action.query)
                is SearchScreenAction.SeeAllClicked -> viewModel.onSeeAllClicked(action.type)
                SearchScreenAction.LoadMoreSeeAll -> viewModel.loadMoreSeeAllItems()
                is SearchScreenAction.SongPressed -> {
                    val tracksList = when {
                        state.searchResultTracks.any { it.id == action.songId } -> state.searchResultTracks
                        state.seeAllTracks.any { it.id == action.songId } -> state.seeAllTracks
                        else -> state.autocompleteTracks
                    }
                    playMusicFromId(musicPlayer, tracksList, action.songId, repository)
                }
                is SearchScreenAction.PlaylistPressed -> viewModel.onPlaylistPressed(action.playlistId, action.playlistIcon, action.playlistTitle, action.playlistCreatedBy)
            }
        }
    )
}

@Composable
actual fun AddPlaylistScreenHost(navController: NavHostController, args: Route.AddPlaylist) {
    val viewModel: AddPlaylistViewModel = koinViewModel()
    val state by viewModel.addPlaylistState.collectAsState()
    CollectNavigationEvents(viewModel, navController)
    AddPlaylistScreen(
        addPlaylistState = state,
        onAction = { action ->
            when (action) {
                is AddPlaylistAction.BackPressed -> if (action.isFromBottomSheet) viewModel.popBack()
                is AddPlaylistAction.AddPlaylistClicked -> viewModel.onAddPlaylistClicked(action.playlistName, action.playlistDescription)
                is AddPlaylistAction.AddSongToPlaylist -> viewModel.onPlaylistItemClicked(action.playlistTitle, action.playlistDescription, action.songs)
            }
        }
    )
}

@Composable
actual fun SettingsScreenHost(navController: NavHostController) {
    val viewModel: SettingsViewModel = koinInject()
    val state by viewModel.settingsState.collectAsState()
    CollectNavigationEvents(viewModel, navController)
    SettingsScreen(
        settings = state,
        updateSettings = { updated ->
            if (updated.isDarkThemeOn != state.isDarkThemeOn) viewModel.onDarkModeToggled(updated.isDarkThemeOn)
            if (updated.hasDonationNavigationOn != state.hasDonationNavigationOn) viewModel.onDonationToggled(updated.hasDonationNavigationOn)
            if (updated.palletColor != state.palletColor) viewModel.onPaletteSelected(updated.palletColor)
        },
        onNavigateToCustomTheme = {
            navController.navigate(Route.CustomTheme)
        }
    )
}

@Composable
actual fun DonationScreenHost(navController: NavHostController) {
    DonationScreen()
}

@Composable
actual fun SeeAllScreenHost(navController: NavHostController, args: Route.SeeAll) {
    val viewModel: SeeAllViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val repository: LibraryRepository = koinInject()
    val state by viewModel.seeAllState.collectAsState()
    LaunchedEffect(args) { viewModel.load(args) }
    CollectNavigationEvents(viewModel, navController)
    SeeAllScreen(
        seeAllState = state,
        onAction = { action ->
            when (action) {
                is SeeAllAction.PlaylistClicked -> viewModel.onPlaylistClicked(action.playlistId, action.playlistIcon, action.createdBy, action.title)
                is SeeAllAction.SongPressed -> playMusicFromId(musicPlayer, state.items, action.songId, repository)
                SeeAllAction.BackPressed -> viewModel.popBack()
                is SeeAllAction.LoadMore -> viewModel.fetchItems(action.offset)
                is SeeAllAction.ChangeTimeRange -> viewModel.changeTimeRange(action.timeRange)
                is SeeAllAction.ChangeGenre -> viewModel.changeGenre(action.genre)
            }
        }
    )
}

@Composable
actual fun MusicInsightsScreenHost(navController: NavHostController) {
    val viewModel: LibraryViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val state by viewModel.libraryState.collectAsState()
    val repository: LibraryRepository = koinInject()
    CollectNavigationEvents(viewModel, navController)
    MusicInsightsScreen(
        libraryState = state,
        musicPlayer = musicPlayer,
        libraryRepository = repository,
        onBackPressed = { viewModel.popBack() },
        onLoadMoreMostPlayed = { currentCount -> viewModel.loadMoreMostPlayedSongs(currentCount) }
    )
}

@Composable
actual fun CustomThemeScreenHost(navController: NavHostController) {
    val viewModel: SettingsViewModel = koinInject()
    val state by viewModel.settingsState.collectAsState()
    CustomThemeScreen(
        settings = state,
        onBackPressed = { navController.popBackStack() },
        onCustomColorsApplied = { primary, secondary, background, surface ->
            viewModel.updateCustomColors(primary, secondary, background, surface)
        },
        onPaletteSelected = { color ->
            viewModel.onPaletteSelected(color)
        }
    )
}
