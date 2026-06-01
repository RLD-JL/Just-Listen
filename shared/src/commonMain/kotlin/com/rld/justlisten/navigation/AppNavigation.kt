package com.rld.justlisten.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute


private fun androidx.navigation.NavBackStackEntry.getTabIndex(): Int {
    val routeStr = destination.route.orEmpty()
    return when {
        routeStr.contains("AddPlaylist") -> 0
        routeStr.contains("Library") -> 0
        routeStr.contains("Playlist") -> 1
        routeStr.contains("Search") -> 2
        routeStr.contains("Donation") -> 3
        routeStr.contains("Settings") -> 4
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
        
        composable<Route.Settings> {
            SettingsScreenHost(navController)
        }
        
        composable<Route.Donation> {
            DonationScreenHost(navController)
        }
    }
}


