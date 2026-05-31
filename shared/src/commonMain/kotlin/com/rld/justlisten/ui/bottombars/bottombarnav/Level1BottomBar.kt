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
    onItemClick: () -> Unit = {}
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    fun isSelected(route: Route): Boolean {
        val label = route::class.simpleName.orEmpty()
        return currentDestination?.route?.contains(label, ignoreCase = true) == true
    }

    fun navigateTo(route: Route) {
        onItemClick()
        navController.navigate(route) {
            popUpTo(Route.Library) { saveState = true }
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
                    if (isSelected(Route.Playlist)) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Playlist",
                )
            },
            label = { Text("Playlist", fontSize = 10.sp) },
            selected = isSelected(Route.Playlist),
            onClick = { navigateTo(Route.Playlist) },
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
                    painter = if (isSelected(Route.Library)) painterResource(Res.drawable.ic_library_music_filled) else painterResource(Res.drawable.ic_library_music_outlined),
                    contentDescription = "Library",
                )
            },
            label = { Text("Library", fontSize = 10.sp) },
            selected = isSelected(Route.Library),
            onClick = { navigateTo(Route.Library) },
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
                    if (isSelected(Route.Search)) Icons.Filled.Search else Icons.Outlined.Search,
                    contentDescription = "Search",
                )
            },
            label = { Text("Search", fontSize = 10.sp) },
            selected = isSelected(Route.Search),
            onClick = { navigateTo(Route.Search) },
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
                        painter = if (isSelected(Route.Donation)) painterResource(Res.drawable.ic_monetization_on_filled) else painterResource(Res.drawable.ic_monetization_on_outlined),
                        contentDescription = "Donate",
                    )
                },
                label = { Text("Support", fontSize = 10.sp) },
                selected = isSelected(Route.Donation),
                onClick = { navigateTo(Route.Donation) },
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
                    if (isSelected(Route.Settings)) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings",
                )
            },
            label = { Text("Settings", fontSize = 10.sp) },
            selected = isSelected(Route.Settings),
            onClick = { navigateTo(Route.Settings) },
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
