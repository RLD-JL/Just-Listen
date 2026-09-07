package com.rld.justlisten.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.ui.actions.ArtistProfileAction
import com.rld.justlisten.ui.actions.FeedAction
import com.rld.justlisten.ui.utils.playMusicFromId
import com.rld.justlisten.viewmodel.artistdashboard.ArtistDashboardViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ArtistProfileScreenHost(
    navController: NavHostController,
    args: Route.ArtistProfile
) {
    val viewModel: com.rld.justlisten.viewmodel.artistprofile.ArtistProfileViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val repository: LibraryRepository = koinInject()
    val state by viewModel.artistProfileState.collectAsState()
    val iosCallbacks = LocalIosNavigationCallbacks.current

    LaunchedEffect(args) { viewModel.load(args) }
    CollectNavigationEvents(viewModel, navController)

    com.rld.justlisten.ui.artistprofile.ArtistProfileScreen(
        artistProfileState = state,
        musicPlayer = musicPlayer,
        libraryRepository = repository,
        onAction = { action ->
            when (action) {
                is ArtistProfileAction.BackPressed -> viewModel.popBack()
                is ArtistProfileAction.SongPressed -> playMusicFromId(
                    musicPlayer,
                    state.artistTracks,
                    action.songId,
                    repository
                )
                is ArtistProfileAction.PlaylistClicked -> viewModel.onPlaylistClicked(
                    action.playlistId,
                    action.playlistIcon,
                    action.createdBy,
                    action.title
                )
                is ArtistProfileAction.FollowPressed -> viewModel.onFollowPressed()
                is ArtistProfileAction.DismissConnectPrompt -> viewModel.dismissConnectPrompt()
                is ArtistProfileAction.ConnectAudiusPressed -> {
                    viewModel.dismissConnectPrompt()
                    if (iosCallbacks != null) {
                        iosCallbacks.onNavigate(Route.Settings)
                    } else {
                        navController.navigate(Route.Settings)
                    }
                }
                is ArtistProfileAction.TabSelected -> viewModel.onTabSelected(action.index)
                is ArtistProfileAction.EditProfileSaved -> viewModel.onEditProfileSaved(
                    action.name,
                    action.bio,
                    action.profilePicUrl,
                    action.coverPhotoUrl,
                    action.location,
                    action.xHandle,
                    action.instagramHandle,
                    action.tiktokHandle,
                    action.website,
                    action.fanClubFlair
                )
                is ArtistProfileAction.FollowersClicked -> viewModel.onFollowersClicked()
                is ArtistProfileAction.FollowingClicked -> viewModel.onFollowingClicked()
                is ArtistProfileAction.DismissSocialSheet -> viewModel.onDismissSocialSheet()
                is ArtistProfileAction.SocialFollowPressed -> viewModel.onSocialFollowPressed(action.userId)
                is ArtistProfileAction.ArtistClicked -> viewModel.onNavigateToArtist(
                    action.userId,
                    action.name
                )
            }
        }
    )
}

@Composable
fun FeedScreenHost(
    navController: NavHostController,
    args: Route.Feed
) {
    val viewModel: com.rld.justlisten.viewmodel.feed.FeedViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val repository: LibraryRepository = koinInject()
    val state by viewModel.feedState.collectAsState()
    val iosCallbacks = LocalIosNavigationCallbacks.current

    LaunchedEffect(args) {
        if (args.category != null || args.timeRange != null) {
            viewModel.loadTrendingWithFilters(args.category, args.timeRange)
        }
    }

    CollectNavigationEvents(viewModel, navController)

    com.rld.justlisten.ui.feedscreen.FeedScreen(
        feedState = state,
        musicPlayer = musicPlayer,
        libraryRepository = repository,
        onAction = { action ->
            when (action) {
                is FeedAction.SongPressed -> playMusicFromId(
                    musicPlayer,
                    state.items,
                    action.songId,
                    repository
                )
                is FeedAction.PlaylistClicked -> viewModel.onPlaylistClicked(
                    action.playlistId,
                    action.playlistIcon,
                    action.createdBy,
                    action.title
                )
                is FeedAction.FavoritePressed -> viewModel.onFavoritePressed(
                    action.songId,
                    action.title,
                    action.user,
                    action.songIcon,
                    action.isFavorite
                )
                is FeedAction.RepostPressed -> viewModel.onRepostPressed(
                    action.itemId,
                    action.isRepost,
                    action.isPlaylist
                )
                is FeedAction.ArtistClicked -> viewModel.onArtistClicked(
                    action.artistId,
                    action.artistName
                )
                FeedAction.Refresh -> viewModel.refreshFeed()
                FeedAction.DismissConnectPrompt -> viewModel.dismissConnectPrompt()
                FeedAction.ConnectAudiusPressed -> {
                    viewModel.dismissConnectPrompt()
                    if (iosCallbacks != null) {
                        iosCallbacks.onNavigate(Route.Settings)
                    } else {
                        navController.navigate(Route.Settings)
                    }
                }
                FeedAction.LoadMore -> viewModel.loadMore()
                is FeedAction.SelectTab -> viewModel.selectTab(action.tab)
                is FeedAction.SetPersonalFilter -> viewModel.setPersonalFilter(action.filter)
                is FeedAction.SetPersonalFormat -> viewModel.setPersonalFormat(action.format)
                is FeedAction.SetTrendingCategory -> viewModel.setTrendingCategory(action.category)
                is FeedAction.SetTrendingTimeRange -> viewModel.setTrendingTimeRange(action.timeRange)
            }
        }
    )
}

@Composable
fun NotificationsScreenHost(navController: NavHostController) {
    com.rld.justlisten.ui.notifications.NotificationScreen(
        onBackClicked = navController::popBackStack,
        onNavigateToArtist = { artistId, artistName ->
            navController.navigate(Route.ArtistProfile(artistId, artistName))
        }
    )
}

@Composable
fun ArtistDashboardScreenHost(navController: NavHostController) {
    val viewModel: ArtistDashboardViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    CollectNavigationEvents(viewModel, navController)

    com.rld.justlisten.ui.artistdashboard.ArtistDashboardScreen(
        state = state,
        onBackPressed = viewModel::handleBack,
        onRetry = viewModel::retry
    )
}
