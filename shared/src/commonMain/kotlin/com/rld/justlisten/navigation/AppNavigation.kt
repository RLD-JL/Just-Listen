package com.rld.justlisten.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import androidx.navigation.NavDestination.Companion.hasRoute
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.savePlaylist
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.updatePlaylistSongs
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height


private fun androidx.navigation.NavBackStackEntry.getTabIndex(): Int {
    val dest = destination
    return when {
        dest.hasRoute<Route.Playlist>() -> 0
        dest.hasRoute<Route.Library>() -> 1
        dest.hasRoute<Route.AddPlaylist>() -> 1
        dest.hasRoute<Route.MusicInsights>() -> 1
        dest.hasRoute<Route.Feed>() -> 2
        dest.hasRoute<Route.Search>() -> 3
        dest.hasRoute<Route.Support>() -> 4
        dest.hasRoute<Route.Settings>() -> 5
        dest.hasRoute<Route.CustomTheme>() -> 5
        else -> -1
    }
}

/**
 * Main navigation graph for the app.
 * Handles routing between all screens using platform-specific screen hosts.
 */
@Composable
fun AppNavigation(
    navController: NavHostController,
    startDestination: Route = Route.Playlist,
    modifier: Modifier = Modifier,
) {
    val localDb: com.rld.justlisten.LocalDb = org.koin.compose.koinInject()
    val playlistRepository: com.rld.justlisten.datalayer.repositories.PlaylistRepository = org.koin.compose.koinInject()
    val musicPlayer: com.rld.justlisten.media.MusicPlayer = org.koin.compose.koinInject()
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    val activeImportPlaylistState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Route.PlaylistDetail?>(null) }
    val activeImportPlaylist = activeImportPlaylistState.value

    val activeImportTrackIdState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val activeImportTrackId = activeImportTrackIdState.value

    val activeImportTrackModelState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<com.rld.justlisten.datalayer.models.PlayListModel?>(null) }
    val activeImportTrackModel = activeImportTrackModelState.value

    val isFetchingTrackDetailsState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    val isFetchingTrackDetails = isFetchingTrackDetailsState.value

    val showCommentsTrackIdState = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    val showCommentsTrackId = showCommentsTrackIdState.value

    androidx.compose.runtime.LaunchedEffect(navController) {
        com.rld.justlisten.util.DeepLinkRouter.deepLinkFlow.collect { url ->
            try {
                val hostPath = url.substringAfter("justlisten://").substringBefore("?")
                val queryString = url.substringAfter("?", "")
                val params = if (queryString.isEmpty()) emptyMap() else {
                    queryString.split("&").mapNotNull {
                        val parts = it.split("=")
                        if (parts.size == 2) parts[0] to parts[1] else null
                    }.toMap()
                }

                if (hostPath == "playlist/import") {
                    val data = params["data"]
                    if (data != null) {
                        val imported = com.rld.justlisten.util.PlaylistShareUtils.importPlaylist(data)
                        if (imported != null) {
                            activeImportPlaylistState.value = imported
                        }
                    }
                } else if (hostPath == "track/share") {
                    val trackId = params["id"]
                    if (trackId != null) {
                        activeImportTrackIdState.value = trackId
                        activeImportTrackModelState.value = null
                        isFetchingTrackDetailsState.value = true
                        launch {
                            val details = playlistRepository.fetchTrackDetails(trackId)
                            activeImportTrackModelState.value = details
                            isFetchingTrackDetailsState.value = false
                        }
                    }
                } else if (hostPath == "comments/share") {
                    val trackId = params["trackId"]
                    if (trackId != null) {
                        showCommentsTrackIdState.value = trackId
                    }
                }
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            val fromIndex = initialState.getTabIndex()
            val toIndex = targetState.getTabIndex()
            if (fromIndex >= 0 && toIndex >= 0 && fromIndex != toIndex) {
                if (toIndex > fromIndex) {
                    fadeIn() + slideInHorizontally(initialOffsetX = { 1000 })
                } else {
                    fadeIn() + slideInHorizontally(initialOffsetX = { -1000 })
                }
            } else {
                fadeIn() + slideInHorizontally(initialOffsetX = { 1000 })
            }
        },
        exitTransition = {
            val fromIndex = initialState.getTabIndex()
            val toIndex = targetState.getTabIndex()
            if (fromIndex >= 0 && toIndex >= 0 && fromIndex != toIndex) {
                if (toIndex > fromIndex) {
                    fadeOut() + slideOutHorizontally(targetOffsetX = { -1000 })
                } else {
                    fadeOut() + slideOutHorizontally(targetOffsetX = { 1000 })
                }
            } else {
                fadeOut() + slideOutHorizontally(targetOffsetX = { -1000 })
            }
        },
        popEnterTransition = {
            val fromIndex = initialState.getTabIndex()
            val toIndex = targetState.getTabIndex()
            if (fromIndex >= 0 && toIndex >= 0 && fromIndex != toIndex) {
                if (toIndex > fromIndex) {
                    fadeIn() + slideInHorizontally(initialOffsetX = { 1000 })
                } else {
                    fadeIn() + slideInHorizontally(initialOffsetX = { -1000 })
                }
            } else {
                fadeIn() + slideInHorizontally(initialOffsetX = { -1000 })
            }
        },
        popExitTransition = {
            val fromIndex = initialState.getTabIndex()
            val toIndex = targetState.getTabIndex()
            if (fromIndex >= 0 && toIndex >= 0 && fromIndex != toIndex) {
                if (toIndex > fromIndex) {
                    fadeOut() + slideOutHorizontally(targetOffsetX = { -1000 })
                } else {
                    fadeOut() + slideOutHorizontally(targetOffsetX = { 1000 })
                }
            } else {
                fadeOut() + slideOutHorizontally(targetOffsetX = { 1000 })
            }
        },
    ) {
        composable<Route.Library> {
            LibraryScreenHost(navController)
        }

        composable<Route.MusicInsights> {
            MusicInsightsScreenHost(navController)
        }
        
        composable<Route.Playlist> {
            PlaylistScreenHost(navController)
        }
        
        composable<Route.PlaylistDetail> { backStackEntry ->
            val args: Route.PlaylistDetail = backStackEntry.toRoute()
            PlaylistDetailScreenHost(navController, args)
        }

        composable<Route.SeeAll> { backStackEntry ->
            val args: Route.SeeAll = backStackEntry.toRoute()
            SeeAllScreenHost(navController, args)
        }
        
        composable<Route.Search> {
            SearchScreenHost(navController)
        }
        
        composable<Route.AddPlaylist> { backStackEntry ->
            val args: Route.AddPlaylist = backStackEntry.toRoute()
            AddPlaylistScreenHost(navController, args)
        }

        composable<Route.Onboarding> {
            val viewModel: SettingsViewModel = org.koin.compose.koinInject()
            com.rld.justlisten.ui.onboardingscreen.OnboardingScreen(
                onProceedAsGuest = {
                    viewModel.completeOnboarding()
                    navController.navigate(Route.Playlist) {
                        popUpTo(Route.Onboarding) { inclusive = true }
                    }
                },
                onAuthenticatePressed = {
                    val redirectUri = "justlisten://oauth/callback"
                    val authUrl = viewModel.getAuthUrl(redirectUri)
                    return@OnboardingScreen authUrl
                }
            )
        }
        
        composable<Route.Settings> {
            SettingsScreenHost(navController)
        }
        
        composable<Route.Support> {
            SupportScreenHost(navController)
        }

        composable<Route.CustomTheme> {
            CustomThemeScreenHost(navController)
        }

        composable<Route.ArtistProfile> { backStackEntry ->
            val args: Route.ArtistProfile = backStackEntry.toRoute()
            ArtistProfileScreenHost(navController, args)
        }

        composable<Route.Feed> { backStackEntry ->
            val args: Route.Feed = backStackEntry.toRoute()
            FeedScreenHost(navController, args)
        }

        composable<Route.Notifications> {
            NotificationsScreenHost(navController)
        }

        composable<Route.ArtistDashboard> {
            ArtistDashboardScreenHost(navController)
        }
    }

    val playlist = activeImportPlaylist
    if (playlist != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { activeImportPlaylistState.value = null },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            title = {
                androidx.compose.material3.Text(
                    text = "Import Playlist",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                androidx.compose.foundation.layout.Column {
                    androidx.compose.material3.Text(
                        text = "Title: ${playlist.playlistTitle}",
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                    androidx.compose.material3.Text(text = "Created by: ${playlist.playlistCreatedBy}")
                    androidx.compose.material3.Text(text = "Tracks: ${playlist.songsList.size}")
                }
            },
            confirmButton = {
                androidx.compose.material3.Button(
                    onClick = {
                        coroutineScope.launch {
                            localDb.savePlaylist(
                                playlistName = playlist.playlistTitle,
                                playlistDescription = "Imported Shared Playlist",
                                playlistId = playlist.playlistId
                            )
                            localDb.updatePlaylistSongs(
                                playlistName = playlist.playlistTitle,
                                playlistDescription = "Imported Shared Playlist",
                                songList = playlist.songsList,
                                playlistId = playlist.playlistId
                            )
                            com.rld.justlisten.ui.utils.showToast("Playlist imported successfully!")
                            navController.navigate(
                                Route.PlaylistDetail(
                                    playlistId = playlist.playlistId,
                                    playlistIcon = playlist.playlistIcon,
                                    playlistTitle = playlist.playlistTitle,
                                    playlistCreatedBy = playlist.playlistCreatedBy,
                                    playlistEnum = "CREATED_BY_USER",
                                    songsList = playlist.songsList
                                )
                            )
                            activeImportPlaylistState.value = null
                        }
                    }
                ) {
                    androidx.compose.material3.Text("Import & View")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { activeImportPlaylistState.value = null }) {
                    androidx.compose.material3.Text("Cancel")
                }
            }
        )
    }

    val trackIdVal = activeImportTrackId
    if (trackIdVal != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { activeImportTrackIdState.value = null },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            title = {
                androidx.compose.material3.Text(
                    text = "Play Shared Track",
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                if (isFetchingTrackDetails) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxWidth().height(60.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator()
                    }
                } else {
                    val track = activeImportTrackModel
                    if (track != null) {
                        androidx.compose.foundation.layout.Column {
                            androidx.compose.material3.Text(
                                text = track.title,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                                fontSize = 16.sp
                            )
                            androidx.compose.material3.Text(text = "by ${track.user.username}")
                        }
                    } else {
                        androidx.compose.material3.Text("Unable to fetch track details. Please check connection.")
                    }
                }
            },
            confirmButton = {
                val track = activeImportTrackModel
                androidx.compose.material3.Button(
                    enabled = track != null,
                    onClick = {
                        track?.let {
                            val playlistItem = com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem(_data = it)
                            musicPlayer.updatePlaylist(listOf(playlistItem))
                            musicPlayer.playMedia(it.id)
                        }
                        activeImportTrackIdState.value = null
                    }
                ) {
                    androidx.compose.material3.Text("Play")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { activeImportTrackIdState.value = null }) {
                    androidx.compose.material3.Text("Cancel")
                }
            }
        )
    }

    val commTrackId = showCommentsTrackId
    if (commTrackId != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showCommentsTrackIdState.value = null },
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            confirmButton = {},
            text = {
                androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth().height(450.dp)) {
                    com.rld.justlisten.ui.bottombars.sheets.components.CommentsView(
                        trackId = commTrackId,
                        onCloseBottomSheet = { showCommentsTrackIdState.value = null }
                    )
                }
            }
        )
    }
}


