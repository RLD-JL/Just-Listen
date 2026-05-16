package com.rld.justlisten.ui.bottombars.bottombarnav

import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rld.justlisten.navigation.Route

@Composable
fun Level1BottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    showDonationTab: Boolean = false,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    fun isSelected(route: Route): Boolean {
        val label = route::class.simpleName.orEmpty()
        return currentDestination?.route?.contains(label, ignoreCase = true) == true
    }

    fun navigateTo(route: Route) {
        navController.navigate(route) {
            popUpTo(Route.Library) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BottomNavigation(
        backgroundColor = MaterialTheme.colors.background,
        modifier = modifier,
    ) {
        BottomNavigationItem(
            icon = {
                Icon(
                    if (isSelected(Route.Playlist)) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Playlist",
                )
            },
            label = { Text("Playlist", fontSize = 10.sp) },
            selected = isSelected(Route.Playlist),
            onClick = { navigateTo(Route.Playlist) },
            selectedContentColor = MaterialTheme.colors.primaryVariant,
            unselectedContentColor = MaterialTheme.colors.onBackground,
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    if (isSelected(Route.Library)) Icons.Filled.LibraryMusic else Icons.Outlined.LibraryMusic,
                    contentDescription = "Library",
                )
            },
            label = { Text("Library", fontSize = 10.sp) },
            selected = isSelected(Route.Library),
            onClick = { navigateTo(Route.Library) },
            selectedContentColor = MaterialTheme.colors.primaryVariant,
            unselectedContentColor = MaterialTheme.colors.onBackground,
        )
        BottomNavigationItem(
            icon = {
                Icon(
                    if (isSelected(Route.Search)) Icons.Filled.Search else Icons.Outlined.Search,
                    contentDescription = "Search",
                )
            },
            label = { Text("Search", fontSize = 10.sp) },
            selected = isSelected(Route.Search),
            onClick = { navigateTo(Route.Search) },
            selectedContentColor = MaterialTheme.colors.primaryVariant,
            unselectedContentColor = MaterialTheme.colors.onBackground,
        )
        if (showDonationTab) {
            BottomNavigationItem(
                icon = {
                    Icon(
                        if (isSelected(Route.Donation)) Icons.Filled.MonetizationOn else Icons.Outlined.MonetizationOn,
                        contentDescription = "Donate",
                    )
                },
                label = { Text("Support", fontSize = 10.sp) },
                selected = isSelected(Route.Donation),
                onClick = { navigateTo(Route.Donation) },
                selectedContentColor = MaterialTheme.colors.primaryVariant,
                unselectedContentColor = MaterialTheme.colors.onBackground,
            )
        }
        BottomNavigationItem(
            icon = {
                Icon(
                    if (isSelected(Route.Settings)) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings",
                )
            },
            label = { Text("Settings", fontSize = 10.sp) },
            selected = isSelected(Route.Settings),
            onClick = { navigateTo(Route.Settings) },
            selectedContentColor = MaterialTheme.colors.primaryVariant,
            unselectedContentColor = MaterialTheme.colors.onBackground,
        )
    }
}
