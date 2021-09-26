package com.example.audius.android.ui.bottombars

import android.media.session.PlaybackState
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.example.audius.android.exoplayer.MusicService
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.theme.graySurface
import com.example.audius.viewmodel.screens.Level1Navigation
import com.example.audius.viewmodel.screens.trending.skipToNextSong

@Composable
fun Navigation.Level1BottomBar(
    selectedTab: ScreenIdentifier,
    musicServiceConnection: MusicServiceConnection
) {
    val bottomNavBackground =
        if (isSystemInDarkTheme()) graySurface else MaterialTheme.colors.background

    Box(modifier = Modifier.fillMaxWidth()) {
        if (musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PLAYING
            || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PAUSED
            || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_SKIPPING_TO_NEXT
            || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_BUFFERING
            || musicServiceConnection.currentPlayingSong.value !=null) {
            val songIcon =
                musicServiceConnection.currentPlayingSong.value?.description?.iconUri.toString()
            val title = musicServiceConnection.currentPlayingSong.value?.description?.title.toString()
            PlayerBottomBar(modifier = Modifier.offset(y = (2).dp), songIcon = songIcon, title = title,
                onSkipNextPressed = {musicServiceConnection.transportControls.skipToNext()},
                musicServiceConnection = musicServiceConnection)
        }

        BottomNavigation(backgroundColor = bottomNavBackground,
            modifier = Modifier.align(Alignment.BottomCenter), content = {
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