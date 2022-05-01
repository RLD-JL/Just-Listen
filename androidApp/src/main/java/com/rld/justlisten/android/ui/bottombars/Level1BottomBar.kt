package com.rld.justlisten.android.ui.bottombars

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.rld.justlisten.Navigation
import com.rld.justlisten.ScreenIdentifier
import com.rld.justlisten.android.R
import com.rld.justlisten.viewmodel.screens.Level1Navigation

@Composable
fun Navigation.Level1BottomBar(
    selectedTab: ScreenIdentifier,
    modifier: Modifier
) {
        BottomNavigation(
            modifier = modifier,
             content = {
            BottomNavigationItem(
                icon = { Icon(Icons.Default.Home, "ALL") },
                label = { Text("Playlist", fontSize = 10.sp) },
                selected = selectedTab.URI == Level1Navigation.Playlist.screenIdentifier.URI,
                onClick = { navigateByLevel1Menu(Level1Navigation.Playlist) }
            )
            BottomNavigationItem(
                icon = { Icon(painter = painterResource(id = R.drawable.ic_baseline_library_music_24), "FAVORITES") },
                label = { Text("Library", fontSize = 10.sp) },
                selected = selectedTab.URI == Level1Navigation.Library.screenIdentifier.URI,
                onClick = { navigateByLevel1Menu(Level1Navigation.Library) }
            )
        })
}