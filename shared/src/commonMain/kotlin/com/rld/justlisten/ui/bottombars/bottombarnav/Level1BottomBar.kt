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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.painterResource
import justlisten.shared.generated.resources.Res
import justlisten.shared.generated.resources.ic_library_music_filled
import justlisten.shared.generated.resources.ic_library_music_outlined
import justlisten.shared.generated.resources.ic_monetization_on_filled
import justlisten.shared.generated.resources.ic_monetization_on_outlined
import justlisten.shared.generated.resources.ic_feed_filled
import justlisten.shared.generated.resources.ic_feed_outlined
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.rld.justlisten.navigation.Route

@Composable
fun Level1BottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    showSupportTab: Boolean = false,
    onItemClick: () -> Unit = {}
) {
    val backStack by navController.currentBackStack.collectAsState()
    // The nearest tab root owns every detail above it, including restored stacks.
    // Do not infer ownership from the last tab tapped or remember a detail route.
    val currentTab = backStack.asReversed().firstNotNullOfOrNull { entry ->
        when {
            entry.destination.hasRoute<Route.Library>() -> "Library"
            entry.destination.hasRoute<Route.Feed>() -> "Feed"
            entry.destination.hasRoute<Route.Settings>() -> "Settings"
            entry.destination.hasRoute<Route.Support>() -> "Support"
            entry.destination.hasRoute<Route.Search>() -> "Search"
            entry.destination.hasRoute<Route.Playlist>() -> "Playlist"
            else -> null
        }
    } ?: "Playlist"

    fun navigateTo(tabName: String, rootRoute: Route) {
        onItemClick()
        if (currentTab == tabName) {
            // Reselecting a tab returns to its existing root without duplicating it.
            navController.popBackStack(rootRoute, inclusive = false)
        } else {
            // Restore through the root so Navigation restores the entire stack:
            // Library -> Favorites, not just Favorites above the Playlist tab.
            navController.navigate(rootRoute) {
                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
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
            onClick = { navigateTo("Playlist", Route.Playlist) },
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
            onClick = { navigateTo("Library", Route.Library) },
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
                    painter = if (currentTab == "Feed") painterResource(Res.drawable.ic_feed_filled) else painterResource(Res.drawable.ic_feed_outlined),
                    contentDescription = "Feed",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Feed", fontSize = 10.sp) },
            selected = currentTab == "Feed",
            onClick = { navigateTo("Feed", Route.Feed()) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onBackground,
                unselectedTextColor = MaterialTheme.colorScheme.onBackground,
                indicatorColor = Color.Transparent
            )
        )
        if (showSupportTab) {
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = if (currentTab == "Support") painterResource(Res.drawable.ic_monetization_on_filled) else painterResource(Res.drawable.ic_monetization_on_outlined),
                        contentDescription = "Support",
                    )
                },
                label = { Text("Support", fontSize = 10.sp) },
                selected = currentTab == "Support",
                onClick = { navigateTo("Support", Route.Support) },
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
            onClick = { navigateTo("Settings", Route.Settings) },
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
