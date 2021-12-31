package com.example.audius.android.ui.bottombars

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import com.example.audius.Navigation
import com.example.audius.ScreenIdentifier
import com.example.audius.android.R
import com.example.audius.viewmodel.screens.Level1Navigation

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