package com.example.audius.android.ui.theme

import android.support.v4.media.MediaBrowserCompat
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import com.example.audius.Navigation
import com.example.audius.ScreenIdentifier
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.exoplayer.utils.Constants.MEDIA_ROOT_ID
import com.example.audius.android.ui.TrendingListScreen
import com.example.audius.viewmodel.screens.Screen
import com.example.audius.viewmodel.screens.trending.playMusic

@Composable
fun Navigation.ScreenPicker(
    screenIdentifier: ScreenIdentifier
) {
    val musicServiceConnection = MusicServiceConnection(LocalContext.current)
    musicServiceConnection.subscribe(MEDIA_ROOT_ID, object: MediaBrowserCompat.SubscriptionCallback() {})
    when (screenIdentifier.screen) {

        Screen.TrendingList ->
            TrendingListScreen(
                musicServiceConnection = musicServiceConnection,
                trendingListState = stateProvider.get(screenIdentifier),
                onLastItemClick = { songId,songIcon ->
                        events.playMusic(songId = songId, songIcon = songIcon)
                },
            )
    }
    }

fun skipToNextSong(musicServiceConnection: MusicServiceConnection) {
    musicServiceConnection.transportControls.skipToNext()
}
