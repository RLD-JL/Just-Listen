package com.rld.justlisten.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute


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
        enterTransition = { fadeIn() + slideInHorizontally(initialOffsetX = { 1000 }) },
        exitTransition = { fadeOut() + slideOutHorizontally(targetOffsetX = { 1000 }) },
        popEnterTransition = { fadeIn() + slideInHorizontally(initialOffsetX = { -1000 }) },
        popExitTransition = { fadeOut() + slideOutHorizontally(targetOffsetX = { -1000 }) },
    ) {
        composable<Route.Library> {
            LibraryScreenHost(navController)
        }
        
        composable<Route.Playlist> {
            PlaylistScreenHost(navController)
        }
        
        composable<Route.PlaylistDetail> { backStackEntry ->
            val args: Route.PlaylistDetail = backStackEntry.toRoute()
            PlaylistDetailScreenHost(navController, args)
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


