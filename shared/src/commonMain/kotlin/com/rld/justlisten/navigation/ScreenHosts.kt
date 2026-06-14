package com.rld.justlisten.navigation

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.repositories.LibraryRepository
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
import com.rld.justlisten.viewmodel.seeall.SeeAllViewModel
import com.rld.justlisten.ui.seeallscreen.SeeAllScreen
import com.rld.justlisten.ui.actions.*
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
                is LibraryScreenAction.ArtistClicked -> viewModel.onArtistClicked(action.artistId, action.artistName)
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
        onLoadMoreMostPlayed = { currentCount -> viewModel.loadMoreMostPlayedSongs(currentCount) }
    )
}

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
                is PlaylistScreenAction.PlaylistClicked -> {
                    viewModel.onPlaylistClicked(
                        action.playlistId, 
                        action.playlistIcon, 
                        action.createdBy, 
                        action.title
                    )
                }
                is PlaylistScreenAction.SongPressed -> {
                    playMusicFromId(musicPlayer, state.tracksList, action.songId, repository)
                }
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
                is PlaylistScreenAction.SeeAllClicked -> {
                    viewModel.onSeeAllClicked(
                        categoryName = action.categoryName,
                        playlistEnum = action.playlistEnum,
                        queryPlaylist = action.queryPlaylist
                    )
                }
                is PlaylistScreenAction.SeeAllTracksClicked -> {
                    viewModel.onSeeAllTracksClicked(
                        categoryName = action.categoryName,
                        queryPlaylist = action.queryPlaylist,
                        selectedTimeRange = action.selectedTimeRange
                    )
                }
                PlaylistScreenAction.NotificationsClicked -> {
                    navController.navigate(Route.Notifications)
                }
                is PlaylistScreenAction.ArtistClicked -> viewModel.onArtistClicked(action.artistId, action.artistName)
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
                is PlaylistDetailAction.DeleteSongFromPlaylist -> viewModel.removeSongFromPlaylist(state.playlistName, action.songId)
                is PlaylistDetailAction.EditPlaylistTitleClicked -> viewModel.editPlaylistTitle(action.oldName, action.newName)
                is PlaylistDetailAction.ArtistClicked -> viewModel.onArtistClicked(action.artistId, action.artistName)
                is PlaylistDetailAction.RepostPressed -> viewModel.onRepostPressed(action.songId, action.isRepost)
                PlaylistDetailAction.DismissConnectPrompt -> viewModel.dismissConnectPrompt()
                PlaylistDetailAction.ConnectAudiusPressed -> {
                    viewModel.dismissConnectPrompt()
                    navController.navigate(Route.Settings)
                }
            }
        }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchScreenHost(navController: NavHostController) {
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
                    playMusicFromId(
                        musicPlayer, 
                        tracksList, 
                        action.songId, 
                        repository
                    )
                }
                is SearchScreenAction.PlaylistPressed -> viewModel.onPlaylistPressed(
                    action.playlistId,
                    action.playlistIcon,
                    action.playlistTitle,
                    action.playlistCreatedBy
                )
                is SearchScreenAction.ArtistClicked -> viewModel.onArtistClicked(action.artistId, action.artistName)
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
fun SettingsScreenHost(navController: NavHostController) {
    val viewModel: SettingsViewModel = koinInject()
    val state by viewModel.settingsState.collectAsState()
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current

    CollectNavigationEvents(viewModel, navController)

    SettingsScreen(
        settings = state,
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
            if (updated.isOngoingStreamEnabled != state.isOngoingStreamEnabled) {
                viewModel.onOngoingStreamToggled(updated.isOngoingStreamEnabled)
            }
            if (updated.isCrossfadeEnabled != state.isCrossfadeEnabled) {
                viewModel.onCrossfadeToggled(updated.isCrossfadeEnabled)
            }
            if (updated.isVolumeNormalizationEnabled != state.isVolumeNormalizationEnabled) {
                viewModel.onVolumeNormalizationToggled(updated.isVolumeNormalizationEnabled)
            }
            if (updated.crossfadeDurationSeconds != state.crossfadeDurationSeconds) {
                viewModel.onCrossfadeDurationChanged(updated.crossfadeDurationSeconds)
            }
            if (updated.crossfadeStyle != state.crossfadeStyle) {
                viewModel.onCrossfadeStyleChanged(updated.crossfadeStyle)
            }
            if (updated.isEqEnabled != state.isEqEnabled ||
                updated.eqPreset != state.eqPreset ||
                updated.eqBands != state.eqBands
            ) {
                viewModel.onEqualizerSettingsChanged(
                    enabled = updated.isEqEnabled,
                    preset = updated.eqPreset,
                    bands = updated.eqBands
                )
            }
        },
        onNavigateToCustomTheme = {
            navController.navigate(com.rld.justlisten.navigation.Route.CustomTheme)
        },
        onLogin = { redirectUri ->
            val authUrl = viewModel.getAuthUrl(redirectUri)
            if (authUrl.isNotBlank()) {
                uriHandler.openUri(authUrl)
            }
        },
        onLogout = {
            viewModel.logout()
        },
        onRetrySync = {
            viewModel.retryFailedSync()
        },
        onClearSync = {
            viewModel.clearFailedSync()
        },
        onNavigateToMyProfile = { userId, name ->
            navController.navigate(com.rld.justlisten.navigation.Route.ArtistProfile(userId, name))
        }
    )
}

@Composable
fun DonationScreenHost(navController: NavHostController) {
    DonationScreen()
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
                is SeeAllAction.ArtistClicked -> viewModel.onArtistClicked(action.artistId, action.artistName)
            }
        }
    )
}

@Composable
fun CustomThemeScreenHost(navController: NavHostController) {
    val viewModel: SettingsViewModel = koinInject()
    val state by viewModel.settingsState.collectAsState()

    com.rld.justlisten.ui.settingsscreen.CustomThemeScreen(
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

@Composable
fun ArtistProfileScreenHost(
    navController: NavHostController,
    args: Route.ArtistProfile
) {
    val viewModel: com.rld.justlisten.viewmodel.artistprofile.ArtistProfileViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val repository: LibraryRepository = koinInject()
    val state by viewModel.artistProfileState.collectAsState()

    LaunchedEffect(args) { viewModel.load(args) }
    CollectNavigationEvents(viewModel, navController)

    com.rld.justlisten.ui.artistprofile.ArtistProfileScreen(
        artistProfileState = state,
        musicPlayer = musicPlayer,
        libraryRepository = repository,
        onAction = { action ->
            when (action) {
                is com.rld.justlisten.ui.actions.ArtistProfileAction.BackPressed -> viewModel.popBack()
                is com.rld.justlisten.ui.actions.ArtistProfileAction.SongPressed -> playMusicFromId(
                    musicPlayer,
                    state.artistTracks,
                    action.songId,
                    repository
                )
                is com.rld.justlisten.ui.actions.ArtistProfileAction.PlaylistClicked -> viewModel.onPlaylistClicked(
                    action.playlistId,
                    action.playlistIcon,
                    action.createdBy,
                    action.title
                )
                is com.rld.justlisten.ui.actions.ArtistProfileAction.FollowPressed -> viewModel.onFollowPressed()
                is com.rld.justlisten.ui.actions.ArtistProfileAction.DismissConnectPrompt -> viewModel.dismissConnectPrompt()
                is com.rld.justlisten.ui.actions.ArtistProfileAction.ConnectAudiusPressed -> {
                    viewModel.dismissConnectPrompt()
                    navController.navigate(Route.Settings)
                }
                is com.rld.justlisten.ui.actions.ArtistProfileAction.TabSelected -> viewModel.onTabSelected(action.index)
                is com.rld.justlisten.ui.actions.ArtistProfileAction.EditProfileSaved -> viewModel.onEditProfileSaved(
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
                is com.rld.justlisten.ui.actions.ArtistProfileAction.FollowersClicked -> viewModel.onFollowersClicked()
                is com.rld.justlisten.ui.actions.ArtistProfileAction.FollowingClicked -> viewModel.onFollowingClicked()
                is com.rld.justlisten.ui.actions.ArtistProfileAction.DismissSocialSheet -> viewModel.onDismissSocialSheet()
                is com.rld.justlisten.ui.actions.ArtistProfileAction.SocialFollowPressed -> viewModel.onSocialFollowPressed(action.userId)
                is com.rld.justlisten.ui.actions.ArtistProfileAction.ArtistClicked -> viewModel.onNavigateToArtist(action.userId, action.name)
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
                is com.rld.justlisten.ui.actions.FeedAction.SongPressed -> playMusicFromId(
                    musicPlayer,
                    state.items,
                    action.songId,
                    repository
                )
                is com.rld.justlisten.ui.actions.FeedAction.PlaylistClicked -> viewModel.onPlaylistClicked(
                    action.playlistId,
                    action.playlistIcon,
                    action.createdBy,
                    action.title
                )
                is com.rld.justlisten.ui.actions.FeedAction.FavoritePressed -> viewModel.onFavoritePressed(
                    action.songId,
                    action.title,
                    action.user,
                    action.songIcon,
                    action.isFavorite
                )
                is com.rld.justlisten.ui.actions.FeedAction.RepostPressed -> viewModel.onRepostPressed(
                    action.itemId,
                    action.isRepost,
                    action.isPlaylist
                )
                is com.rld.justlisten.ui.actions.FeedAction.ArtistClicked -> viewModel.onArtistClicked(
                    action.artistId,
                    action.artistName
                )
                com.rld.justlisten.ui.actions.FeedAction.Refresh -> viewModel.refreshFeed()
                com.rld.justlisten.ui.actions.FeedAction.DismissConnectPrompt -> viewModel.dismissConnectPrompt()
                com.rld.justlisten.ui.actions.FeedAction.ConnectAudiusPressed -> {
                    viewModel.dismissConnectPrompt()
                    navController.navigate(Route.Settings)
                }
                com.rld.justlisten.ui.actions.FeedAction.LoadMore -> viewModel.loadMore()
                is com.rld.justlisten.ui.actions.FeedAction.SelectTab -> viewModel.selectTab(action.tab)
                is com.rld.justlisten.ui.actions.FeedAction.SetPersonalFilter -> viewModel.setPersonalFilter(action.filter)
                is com.rld.justlisten.ui.actions.FeedAction.SetPersonalFormat -> viewModel.setPersonalFormat(action.format)
                is com.rld.justlisten.ui.actions.FeedAction.SetTrendingCategory -> viewModel.setTrendingCategory(action.category)
                is com.rld.justlisten.ui.actions.FeedAction.SetTrendingTimeRange -> viewModel.setTrendingTimeRange(action.timeRange)
            }
        }
    )
}

@Composable
fun NotificationsScreenHost(navController: NavHostController) {
    com.rld.justlisten.ui.notifications.NotificationScreen(
        onBackClicked = {
            navController.popBackStack()
        },
        onNavigateToArtist = { artistId, artistName ->
            navController.navigate(Route.ArtistProfile(artistId, artistName))
        }
    )
}
