package com.example.audius.android.ui.bottombars

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.BottomAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audius.Navigation
import com.example.audius.ScreenIdentifier
import com.example.audius.viewmodel.screens.Level1Navigation

@Composable
fun Navigation.Level1BottomBar(
    selectedTab: ScreenIdentifier
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        PlayerBottomBar(modifier = Modifier, songIcon = "", title ="yolooo" ) {
        }

        BottomNavigation(modifier = Modifier.align(Alignment.BottomCenter), content = {
            BottomNavigationItem(
                icon = { Icon(Icons.Default.Menu, "ALL") },
                label = { Text("Playlist", fontSize = 13.sp) },
                selected = selectedTab.URI == Level1Navigation.Playlist.screenIdentifier.URI,
                onClick = { navigateByLevel1Menu(Level1Navigation.Playlist) }
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Default.Star, "FAVORITES") },
                label = { Text("Favourites", fontSize = 13.sp) },
                selected = selectedTab.URI == Level1Navigation.AllTrending.screenIdentifier.URI,
                onClick = { navigateByLevel1Menu(Level1Navigation.AllTrending) }
            )
        })
    }
}