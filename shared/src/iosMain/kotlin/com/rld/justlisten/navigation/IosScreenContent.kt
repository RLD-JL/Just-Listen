package com.rld.justlisten.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

/** Routes a SwiftUI navigation destination to its shared Compose screen. */
@Composable
internal fun IosScreenContent(route: Route, navController: NavHostController) {
    val iosCallbacks = LocalIosNavigationCallbacks.current
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
        is Route.Comments -> {
            com.rld.justlisten.ui.bottombars.sheets.components.CommentsView(
                trackId = route.trackId,
                targetCommentId = route.targetCommentId,
                onCloseBottomSheet = {
                    iosCallbacks?.onPopBackStack?.invoke() ?: navController.popBackStack()
                },
                onUserProfileClick = { userId, userName ->
                    val destination = Route.ArtistProfile(userId, userName)
                    iosCallbacks?.onNavigate?.invoke(destination)
                        ?: navController.navigate(destination)
                },
            )
        }
        Route.Notifications -> NotificationsScreenHost(navController)
        Route.ArtistDashboard -> ArtistDashboardScreenHost(navController)
        Route.CustomTheme -> CustomThemeScreenHost(navController)
        Route.MusicInsights -> MusicInsightsScreenHost(navController)
        Route.Onboarding -> Unit
    }
}
