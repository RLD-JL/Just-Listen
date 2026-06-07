package com.rld.justlisten.navigation

import kotlinx.serialization.Serializable

/**
 * Sealed class hierarchy for all app routes.
 * Uses kotlinx.serialization for type-safe route passing.
 */
@Serializable
sealed class Route {
    
    @Serializable
    data object Library : Route()

    @Serializable
    data object MusicInsights : Route()
    
    @Serializable
    data object Playlist : Route()

    @Serializable
    data object Feed : Route()
    
    @Serializable
    data class PlaylistDetail(
        val playlistId: String,
        val playlistIcon: String,
        val playlistTitle: String,
        val playlistCreatedBy: String,
        val playlistEnum: String,
        val songsList: List<String> = emptyList()
    ) : Route()
    
    @Serializable
    data class AddPlaylist(val initialData: String = "") : Route()
    
    @Serializable
    data class SeeAll(
        val categoryName: String,
        val playlistEnum: String,
        val queryPlaylist: String = "",
        val selectedTimeRange: String = "WEEK"
    ) : Route()
    
    @Serializable
    data object Search : Route()
    
    @Serializable
    data object Settings : Route()
    
    @Serializable
    data object Donation : Route()

    @Serializable
    data object CustomTheme : Route()

    @Serializable
    data object Onboarding : Route()

    @Serializable
    data class ArtistProfile(
        val artistId: String,
        val artistName: String
    ) : Route()
}

/**
 * Navigation levels for managing bottom bar vs modal screens
 */
enum class NavigationLevel {
    LEVEL_1,   // Bottom bar tabs
    LEVEL_2,   // Modal/Detail screens
    LEVEL_3    // Nested modals
}

/**
 * Extension to determine navigation level for each route
 */
val Route.navigationLevel: NavigationLevel
    get() = when (this) {
        Route.Library,
        Route.Playlist,
        Route.Feed,
        Route.Search,
        Route.Settings,
        Route.Donation -> NavigationLevel.LEVEL_1
        
        Route.Onboarding,
        Route.MusicInsights,
        is Route.AddPlaylist,
        is Route.SeeAll,
        Route.CustomTheme,
        is Route.PlaylistDetail,
        is Route.ArtistProfile -> NavigationLevel.LEVEL_2
    }

/**
 * List of routes shown in bottom bar
 */
val BOTTOM_BAR_ROUTES = listOf(
    Route.Library,
    Route.Playlist,
    Route.Feed,
    Route.Settings,
    Route.Donation,
)

/**
 * Helper function to get title for bottom bar
 */
fun Route.getBottomBarLabel(): String = when (this) {
    Route.Library -> "Library"
    Route.Playlist -> "Playlists"
    Route.Feed -> "Feed"
    Route.Settings -> "Settings"
    Route.Donation -> "Donate"
    else -> ""
}

/**
 * Get icon for bottom bar (to be implemented with Material Icons)
 */
fun Route.getBottomBarIcon(): String = when (this) {
    Route.Library -> "library"
    Route.Playlist -> "playlist"
    Route.Feed -> "feed"
    Route.Settings -> "settings"
    Route.Donation -> "favorite"
    else -> ""
}
