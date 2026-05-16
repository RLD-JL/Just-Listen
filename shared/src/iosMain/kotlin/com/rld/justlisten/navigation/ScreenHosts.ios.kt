package com.rld.justlisten.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

/**
 * iOS platform-specific screen host implementations.
 * Currently using placeholder implementations.
 */

@Composable
actual fun LibraryScreenHost(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Library Screen - iOS")
    }
}

@Composable
actual fun PlaylistScreenHost(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Playlist Screen - iOS")
    }
}

@Composable
actual fun PlaylistDetailScreenHost(
    navController: NavHostController,
    args: Route.PlaylistDetail,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Playlist Detail Screen - iOS")
    }
}

@Composable
actual fun SearchScreenHost(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Search Screen - iOS")
    }
}

@Composable
actual fun AddPlaylistScreenHost(
    navController: NavHostController,
    args: Route.AddPlaylist,
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Add Playlist Screen - iOS")
    }
}

@Composable
actual fun SettingsScreenHost(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Settings Screen - iOS")
    }
}

@Composable
actual fun DonationScreenHost(navController: NavHostController) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Donation Screen - iOS")
    }
}

