package com.rld.justlisten.ui.bottombars.bottombarnav

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.painterResource
import justlisten.shared.generated.resources.Res
import justlisten.shared.generated.resources.ic_library_music_filled
import justlisten.shared.generated.resources.ic_library_music_outlined
import justlisten.shared.generated.resources.ic_monetization_on_filled
import justlisten.shared.generated.resources.ic_monetization_on_outlined
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.rld.justlisten.navigation.Route

@Composable
fun Level1BottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    showDonationTab: Boolean = false,
    onItemClick: () -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Track the last active route for each bottom bar tab
    val lastActiveRoutes = remember {
        mutableMapOf<String, Route>(
            "Playlist" to Route.Playlist,
            "Library" to Route.Library,
            "Search" to Route.Search,
            "Settings" to Route.Settings,
            "Donation" to Route.Donation
        )
    }

    var currentTab by remember { mutableStateOf("Playlist") }

    // Safely extract the current type-safe route object from the backstack entry
    val currentRoute = navBackStackEntry?.let { entry ->
        val routeStr = entry.destination.route.orEmpty()
        when {
            routeStr.contains("PlaylistDetail") -> {
                try {
                    entry.toRoute<Route.PlaylistDetail>()
                } catch (e: Exception) {
                    null
                }
            }
            routeStr.contains("AddPlaylist") -> {
                try {
                    entry.toRoute<Route.AddPlaylist>()
                } catch (e: Exception) {
                    null
                }
            }
            routeStr.contains("Playlist") -> Route.Playlist
            routeStr.contains("Library") -> Route.Library
            routeStr.contains("Search") -> Route.Search
            routeStr.contains("Settings") -> Route.Settings
            routeStr.contains("Donation") -> Route.Donation
            else -> null
        }
    }

    // Keep track of active tabs and update the last active route for the current tab
    LaunchedEffect(currentRoute) {
        if (currentRoute != null) {
            when (currentRoute) {
                is Route.Playlist -> {
                    currentTab = "Playlist"
                    lastActiveRoutes["Playlist"] = Route.Playlist
                }
                is Route.Library -> {
                    currentTab = "Library"
                    lastActiveRoutes["Library"] = Route.Library
                }
                is Route.Search -> {
                    currentTab = "Search"
                    lastActiveRoutes["Search"] = Route.Search
                }
                is Route.Settings -> {
                    currentTab = "Settings"
                    lastActiveRoutes["Settings"] = Route.Settings
                }
                is Route.Donation -> {
                    currentTab = "Donation"
                    lastActiveRoutes["Donation"] = Route.Donation
                }
                is Route.PlaylistDetail -> {
                    lastActiveRoutes[currentTab] = currentRoute
                }
                is Route.AddPlaylist -> {
                    lastActiveRoutes[currentTab] = currentRoute
                }
            }
        }
    }

    fun navigateTo(tabName: String, defaultRoute: Route) {
        onItemClick()
        currentTab = tabName
        val routeToNavigate = lastActiveRoutes[tabName] ?: defaultRoute
        navController.navigate(routeToNavigate) {
            popUpTo(Route.Playlist) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = modifier,
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    if (currentTab == "Playlist") Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Playlist",
                )
            },
            label = { Text("Playlist", fontSize = 10.sp) },
            selected = currentTab == "Playlist",
            onClick = {
                if (currentTab == "Playlist") {
                    val isAtRoot = currentDestination?.route?.split('/')?.firstOrNull() == Route.Playlist::class.qualifiedName
                    if (!isAtRoot) {
                        navController.navigate(Route.Playlist) {
                            popUpTo(Route.Playlist) { inclusive = true }
                        }
                    }
                } else {
                    navigateTo("Playlist", Route.Playlist)
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = if (currentTab == "Library") painterResource(Res.drawable.ic_library_music_filled) else painterResource(Res.drawable.ic_library_music_outlined),
                    contentDescription = "Library",
                )
            },
            label = { Text("Library", fontSize = 10.sp) },
            selected = currentTab == "Library",
            onClick = {
                if (currentTab == "Library") {
                    val isAtRoot = currentDestination?.route?.split('/')?.firstOrNull() == Route.Library::class.qualifiedName
                    if (!isAtRoot) {
                        navController.navigate(Route.Library) {
                            popUpTo(Route.Library) { inclusive = true }
                        }
                    }
                } else {
                    navigateTo("Library", Route.Library)
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                indicatorColor = Color.Transparent
            )
        )
        NavigationBarItem(
            icon = {
                Icon(
                    if (currentTab == "Search") Icons.Filled.Search else Icons.Outlined.Search,
                    contentDescription = "Search",
                )
            },
            label = { Text("Search", fontSize = 10.sp) },
            selected = currentTab == "Search",
            onClick = {
                if (currentTab == "Search") {
                    val isAtRoot = currentDestination?.route?.split('/')?.firstOrNull() == Route.Search::class.qualifiedName
                    if (!isAtRoot) {
                        navController.navigate(Route.Search) {
                            popUpTo(Route.Search) { inclusive = true }
                        }
                    }
                } else {
                    navigateTo("Search", Route.Search)
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                indicatorColor = Color.Transparent
            )
        )
        if (showDonationTab) {
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = if (currentTab == "Donation") painterResource(Res.drawable.ic_monetization_on_filled) else painterResource(Res.drawable.ic_monetization_on_outlined),
                        contentDescription = "Donate",
                    )
                },
                label = { Text("Support", fontSize = 10.sp) },
                selected = currentTab == "Donation",
                onClick = {
                    if (currentTab == "Donation") {
                        val isAtRoot = currentDestination?.route?.split('/')?.firstOrNull() == Route.Donation::class.qualifiedName
                        if (!isAtRoot) {
                            navController.navigate(Route.Donation) {
                                popUpTo(Route.Donation) { inclusive = true }
                            }
                        }
                    } else {
                        navigateTo("Donation", Route.Donation)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                    unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                    indicatorColor = Color.Transparent
                )
            )
        }
        NavigationBarItem(
            icon = {
                Icon(
                    if (currentTab == "Settings") Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings",
                )
            },
            label = { Text("Settings", fontSize = 10.sp) },
            selected = currentTab == "Settings",
            onClick = {
                if (currentTab == "Settings") {
                    val isAtRoot = currentDestination?.route?.split('/')?.firstOrNull() == Route.Settings::class.qualifiedName
                    if (!isAtRoot) {
                        navController.navigate(Route.Settings) {
                            popUpTo(Route.Settings) { inclusive = true }
                        }
                    }
                } else {
                    navigateTo("Settings", Route.Settings)
                }
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                indicatorColor = Color.Transparent
            )
        )
    }
}


