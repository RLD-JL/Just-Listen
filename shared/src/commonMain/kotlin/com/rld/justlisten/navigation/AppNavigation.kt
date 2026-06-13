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


private fun androidx.navigation.NavBackStackEntry.getTabIndex(): Int {
    val dest = destination
    return when {
        dest.hasRoute<Route.Playlist>() -> 0
        dest.hasRoute<Route.Library>() -> 1
        dest.hasRoute<Route.AddPlaylist>() -> 1
        dest.hasRoute<Route.MusicInsights>() -> 1
        dest.hasRoute<Route.Feed>() -> 2
        dest.hasRoute<Route.Search>() -> 3
        dest.hasRoute<Route.Donation>() -> 4
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
        
        composable<Route.Donation> {
            DonationScreenHost(navController)
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
    }
}


