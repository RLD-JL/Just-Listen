package com.rld.justlisten.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.ui.actions.AddPlaylistAction
import com.rld.justlisten.ui.actions.PlaylistDetailAction
import com.rld.justlisten.ui.actions.PlaylistScreenAction
import com.rld.justlisten.ui.actions.SeeAllAction
import com.rld.justlisten.ui.addplaylistscreen.AddPlaylistScreen
import com.rld.justlisten.ui.playlistdetailscreen.PlaylistDetailScreen
import com.rld.justlisten.ui.playlistscreen.PlaylistScreen
import com.rld.justlisten.ui.seeallscreen.SeeAllScreen
import com.rld.justlisten.ui.utils.playMusicFromId
import com.rld.justlisten.viewmodel.addplaylist.AddPlaylistViewModel
import com.rld.justlisten.viewmodel.playlist.PlaylistViewModel
import com.rld.justlisten.viewmodel.playlistdetail.PlaylistDetailViewModel
import com.rld.justlisten.viewmodel.seeall.SeeAllViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PlaylistScreenHost(navController: NavHostController) {
    val viewModel: PlaylistViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val repository: LibraryRepository = koinInject()
    val state by viewModel.playlistState.collectAsState()

    CollectNavigationEvents(viewModel, navController)

    PlaylistScreen(
        playlistState = state,
        onAction = { action ->
            when (action) {
                is PlaylistScreenAction.PlaylistClicked -> viewModel.onPlaylistClicked(
                    action.playlistId,
                    action.playlistIcon,
                    action.createdBy,
                    action.title
                )
                is PlaylistScreenAction.SongPressed -> playMusicFromId(
                    musicPlayer,
                    state.tracksList,
                    action.songId,
                    repository
                )
                PlaylistScreenAction.SearchClicked -> viewModel.onSearchClicked()
                PlaylistScreenAction.RefreshScreen -> viewModel.refreshScreen()
                is PlaylistScreenAction.FetchMorePlaylists -> viewModel.fetchPlaylist(
                    action.index,
                    action.category,
                    action.query
                )
                is PlaylistScreenAction.ChangeTracksCategory -> viewModel.getNewTracks(
                    action.category,
                    action.timeRange
                )
                is PlaylistScreenAction.SeeAllClicked -> viewModel.onSeeAllClicked(
                    categoryName = action.categoryName,
                    playlistEnum = action.playlistEnum,
                    queryPlaylist = action.queryPlaylist
                )
                is PlaylistScreenAction.SeeAllTracksClicked -> viewModel.onSeeAllTracksClicked(
                    categoryName = action.categoryName,
                    queryPlaylist = action.queryPlaylist,
                    selectedTimeRange = action.selectedTimeRange
                )
                PlaylistScreenAction.NotificationsClicked -> navController.navigate(Route.Notifications)
                is PlaylistScreenAction.ArtistClicked -> viewModel.onArtistClicked(
                    action.artistId,
                    action.artistName
                )
            }
        }
    )
}

@Composable
fun PlaylistDetailScreenHost(
    navController: NavHostController,
    args: Route.PlaylistDetail,
) {
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
                is PlaylistDetailAction.SongPressed -> playMusicFromId(
                    musicPlayer,
                    state.songPlaylist,
                    action.songId,
                    repository,
                    state.playlistId
                )
                is PlaylistDetailAction.FavoritePressed -> {
                    viewModel.onFavoritePressed(
                        action.songId,
                        action.title,
                        action.user,
                        action.songIcon,
                        action.isFavorite
                    )
                    musicPlayer.refreshMetadata()
                }
                is PlaylistDetailAction.DeletePlaylistClicked -> viewModel.deletePlaylist(action.playlistName)
                is PlaylistDetailAction.DeleteSongFromPlaylist -> viewModel.removeSongFromPlaylist(
                    state.playlistName,
                    action.songId
                )
                is PlaylistDetailAction.EditPlaylistTitleClicked -> viewModel.editPlaylistTitle(
                    action.oldName,
                    action.newName
                )
                is PlaylistDetailAction.ArtistClicked -> viewModel.onArtistClicked(
                    action.artistId,
                    action.artistName
                )
                is PlaylistDetailAction.RepostPressed -> viewModel.onRepostPressed(
                    action.songId,
                    action.isRepost
                )
                PlaylistDetailAction.DismissConnectPrompt -> viewModel.dismissConnectPrompt()
                PlaylistDetailAction.ConnectAudiusPressed -> {
                    viewModel.dismissConnectPrompt()
                    navController.navigate(Route.Settings)
                }
            }
        }
    )
}

@Composable
fun AddPlaylistScreenHost(
    navController: NavHostController,
    args: Route.AddPlaylist,
) {
    val viewModel: AddPlaylistViewModel = koinViewModel()
    val state by viewModel.addPlaylistState.collectAsState()

    CollectNavigationEvents(viewModel, navController)

    AddPlaylistScreen(
        addPlaylistState = state,
        onAction = { action ->
            when (action) {
                is AddPlaylistAction.BackPressed -> if (action.isFromBottomSheet) viewModel.popBack()
                is AddPlaylistAction.AddPlaylistClicked -> viewModel.onAddPlaylistClicked(
                    action.playlistName,
                    action.playlistDescription
                )
                is AddPlaylistAction.AddSongToPlaylist -> viewModel.onPlaylistItemClicked(
                    action.playlistTitle,
                    action.playlistDescription,
                    action.songs
                )
            }
        }
    )
}

@Composable
fun SeeAllScreenHost(
    navController: NavHostController,
    args: Route.SeeAll,
) {
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
                is SeeAllAction.PlaylistClicked -> viewModel.onPlaylistClicked(
                    action.playlistId,
                    action.playlistIcon,
                    action.createdBy,
                    action.title
                )
                is SeeAllAction.SongPressed -> playMusicFromId(
                    musicPlayer,
                    state.items,
                    action.songId,
                    repository
                )
                SeeAllAction.BackPressed -> viewModel.popBack()
                is SeeAllAction.LoadMore -> viewModel.fetchItems(action.offset)
                is SeeAllAction.ArtistClicked -> viewModel.onArtistClicked(
                    action.artistId,
                    action.artistName
                )
            }
        }
    )
}
