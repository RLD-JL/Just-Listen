package com.example.audius.android.ui.bottombars

import android.media.session.PlaybackState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
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
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.viewmodel.screens.Level1Navigation

@Composable
fun Navigation.Level1BottomBar(
    selectedTab: ScreenIdentifier,
    musicServiceConnection: MusicServiceConnection
) {
        BottomNavigation(
             content = {
            BottomNavigationItem(
                icon = { Icon(Icons.Default.Menu, "ALL") },
                label = { Text("Playlist", fontSize = 10.sp) },
                selected = selectedTab.URI == Level1Navigation.Playlist.screenIdentifier.URI,
                onClick = { navigateByLevel1Menu(Level1Navigation.Playlist) }
            )
            BottomNavigationItem(
                icon = { Icon(Icons.Default.Star, "FAVORITES") },
                label = { Text("Favourites", fontSize = 10.sp) },
                selected = selectedTab.URI == Level1Navigation.AllTrending.screenIdentifier.URI,
                onClick = { navigateByLevel1Menu(Level1Navigation.AllTrending) }
            )
        })
}