package com.rld.justlisten.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

@Composable
fun IosScreenContent(route: Route, navController: NavHostController) {
    when (route) {
        Route.Library -> LibraryScreenHost(navController)
        Route.Playlist -> PlaylistScreenHost(navController)
        is Route.Feed -> FeedScreenHost(navController, route)
        Route.Search -> SearchScreenHost(navController)
        Route.Settings -> SettingsScreenHost(navController)
        Route.Support -> SupportScreenHost(navController)

        is Route.PlaylistDetail -> PlaylistDetailScreenHost(navController, route)
        is Route.AddPlaylist -> AddPlaylistScreenHost(navController, route)
        is Route.SeeAll -> SeeAllScreenHost(navController, route)
        is Route.ArtistProfile -> ArtistProfileScreenHost(navController, route)
        Route.Notifications -> NotificationsScreenHost(navController)
        Route.ArtistDashboard -> ArtistDashboardScreenHost(navController)
        Route.CustomTheme -> CustomThemeScreenHost(navController)
        Route.MusicInsights -> MusicInsightsScreenHost(navController)
        Route.Onboarding -> {
            // Render Onboarding Screen directly if needed
        }
    }
}
