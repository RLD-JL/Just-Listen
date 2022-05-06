package com.rld.justlisten.android.ui.bottombars

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.Navigation
import com.rld.justlisten.ScreenIdentifier
import com.rld.justlisten.android.R
import com.rld.justlisten.android.ui.theme.Shapes
import com.rld.justlisten.viewmodel.screens.Level1Navigation

@Composable
fun Navigation.Level1BottomBar(
    selectedTab: ScreenIdentifier,
    modifier: Modifier,
    hasNavigationFundOn: Boolean
) {
    BottomNavigation(
        backgroundColor = MaterialTheme.colors.background,
        modifier = modifier,
        content = {
            val homeSelected = selectedTab.URI == Level1Navigation.Playlist.screenIdentifier.URI
            val librarySelected = selectedTab.URI == Level1Navigation.Library.screenIdentifier.URI
            val fundSelected = selectedTab.URI == Level1Navigation.Fund.screenIdentifier.URI
            val settingsSelected = selectedTab.URI == Level1Navigation.Settings.screenIdentifier.URI
            BottomNavigationItem(
                icon = { if (homeSelected) Icon(Icons.Filled.Home, "Playlist") else
                    Icon(Icons.Outlined.Home, "Playlist")},
                label = { Text("Playlist", fontSize = 10.sp) },
                selected = homeSelected,
                onClick = { navigateByLevel1Menu(Level1Navigation.Playlist) },
                selectedContentColor = MaterialTheme.colors.primaryVariant,
                unselectedContentColor = MaterialTheme.colors.onBackground
            )
            BottomNavigationItem(
                icon = { if(librarySelected) Icon(painter = painterResource(id = R.drawable.ic_baseline_library_music_24),
                    "Library")
                       else  Icon(painter = painterResource(id = R.drawable.ic_outline_library_music_24),
                    "Library")},
                label = { Text("Library", fontSize = 10.sp) },
                selected = selectedTab.URI == Level1Navigation.Library.screenIdentifier.URI,
                onClick = { navigateByLevel1Menu(Level1Navigation.Library) },
                selectedContentColor = MaterialTheme.colors.primaryVariant,
                unselectedContentColor = MaterialTheme.colors.onBackground
            )
            if (hasNavigationFundOn) {
                BottomNavigationItem(
                    icon = { Icon(Icons.Default.Home, "Fund") },
                    label = { Text("Fund", fontSize = 10.sp) },
                    selected = selectedTab.URI == Level1Navigation.Playlist.screenIdentifier.URI,
                    onClick = { navigateByLevel1Menu(Level1Navigation.Playlist) },
                    selectedContentColor = MaterialTheme.colors.primaryVariant,
                    unselectedContentColor = MaterialTheme.colors.onBackground
                )
            }
            BottomNavigationItem(
                icon = { if (settingsSelected) Icon(imageVector = Icons.Filled.Settings, "Settings")
                       else Icon(imageVector = Icons.Outlined.Settings, "Settings") },
                label = { Text("Settings", fontSize = 10.sp) },
                selected = selectedTab.URI == Level1Navigation.Settings.screenIdentifier.URI,
                onClick = { navigateByLevel1Menu(Level1Navigation.Settings) },
                selectedContentColor = MaterialTheme.colors.primaryVariant,
                unselectedContentColor = MaterialTheme.colors.onBackground
            )
        })
}

@Composable
fun IconHolder(selected: Boolean, content: @Composable () -> Unit) {
    BoxWithConstraints(
        if (selected) Modifier.background(
            MaterialTheme.colors.secondaryVariant,
            shape = RectangleShape
        ) else Modifier
    ) {
        content()
    }
}