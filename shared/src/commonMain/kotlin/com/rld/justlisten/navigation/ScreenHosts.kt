package com.rld.justlisten.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavBackStackEntry

@Composable
expect fun LibraryScreenHost(navController: NavHostController)

@Composable
expect fun MusicInsightsScreenHost(navController: NavHostController)

@Composable
expect fun PlaylistScreenHost(navController: NavHostController)

@Composable
expect fun PlaylistDetailScreenHost(
    navController: NavHostController,
    args: Route.PlaylistDetail,
)

@Composable
expect fun SearchScreenHost(navController: NavHostController)

@Composable
expect fun AddPlaylistScreenHost(
    navController: NavHostController,
    args: Route.AddPlaylist,
)

@Composable
expect fun SettingsScreenHost(navController: NavHostController)

@Composable
expect fun DonationScreenHost(navController: NavHostController)

@Composable
expect fun SeeAllScreenHost(
    navController: NavHostController,
    args: Route.SeeAll,
)

@Composable
expect fun CustomThemeScreenHost(navController: NavHostController)

@Composable
expect fun ArtistProfileScreenHost(
    navController: NavHostController,
    args: Route.ArtistProfile
)

@Composable
expect fun FeedScreenHost(navController: NavHostController)
