package com.rld.justlisten.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.ui.actions.LibraryScreenAction
import com.rld.justlisten.ui.libraryscreen.LibraryScreen
import com.rld.justlisten.viewmodel.library.LibraryViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibraryScreenHost(navController: NavHostController) {
    val viewModel: LibraryViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val state by viewModel.libraryState.collectAsState()

    CollectNavigationEvents(viewModel, navController)

    LibraryScreen(
        musicPlayer = musicPlayer,
        libraryState = state,
        onAction = { action ->
            when (action) {
                is LibraryScreenAction.FavoritePlaylistPressed -> viewModel.onFavoritePlaylistClicked(
                    action.playlistId,
                    action.playlistIcon,
                    action.playlistTitle,
                    action.playlistCreatedBy
                )
                is LibraryScreenAction.MostPlayedPlaylistPressed -> viewModel.onMostPlayedPlaylistClicked(
                    action.playlistId,
                    action.playlistIcon,
                    action.playlistTitle,
                    action.playlistCreatedBy
                )
                LibraryScreenAction.PlayListViewClicked -> viewModel.onAddPlaylistClicked()
                is LibraryScreenAction.PlaylistCreatedClicked -> viewModel.onPlaylistCreatedClicked(
                    action.title,
                    action.description,
                    action.songs,
                    action.isRemote,
                    action.isPrivate
                )
                is LibraryScreenAction.DeletePlaylistClicked -> viewModel.deletePlaylist(action.playlistName)
                is LibraryScreenAction.LastItemReached -> viewModel.loadMoreRecentSongs(action.index)
                LibraryScreenAction.TimeCapsulePressed -> viewModel.onTimeCapsuleClicked()
                LibraryScreenAction.ExploreMusicPressed -> viewModel.onExploreMusicClicked()
                LibraryScreenAction.MusicInsightsPressed -> viewModel.onMusicInsightsClicked()
                LibraryScreenAction.ArtistDashboardPressed -> viewModel.onArtistDashboardClicked()
                is LibraryScreenAction.ArtistClicked -> viewModel.onArtistClicked(
                    action.artistId,
                    action.artistName
                )
            }
        }
    )
}

@Composable
fun MusicInsightsScreenHost(navController: NavHostController) {
    val viewModel: LibraryViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val state by viewModel.libraryState.collectAsState()
    val repository: LibraryRepository = koinInject()

    CollectNavigationEvents(viewModel, navController)

    com.rld.justlisten.ui.libraryscreen.MusicInsightsScreen(
        libraryState = state,
        musicPlayer = musicPlayer,
        libraryRepository = repository,
        onBackPressed = { viewModel.popBack() },
        onLoadMoreMostPlayed = viewModel::loadMoreMostPlayedSongs
    )
}
