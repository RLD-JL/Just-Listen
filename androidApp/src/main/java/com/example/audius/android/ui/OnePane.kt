package com.example.audius.android.ui

import android.media.session.PlaybackState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audius.Navigation
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.bottombars.Level1BottomBar
import com.example.audius.android.ui.bottombars.PlayerBottomBar
import com.example.audius.android.ui.screenpicker.ScreenPicker

@Composable
fun Navigation.OnePane(
    saveableStateHolder: SaveableStateHolder,
    musicServiceConnection: MusicServiceConnection
) {
    Scaffold(
        bottomBar = {
            if (currentScreenIdentifier.screen.navigationLevel == 1) Level1BottomBar(
                currentScreenIdentifier, musicServiceConnection
            )
        },
        content = {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(bottom = it.calculateBottomPadding())) {

                Column(modifier = Modifier.weight(1f)) {
                    saveableStateHolder.SaveableStateProvider(currentScreenIdentifier.URI) {
                        ScreenPicker(currentScreenIdentifier, musicServiceConnection)
                    }
                }

                if (musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PLAYING
                    || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PAUSED
                    || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_SKIPPING_TO_NEXT
                    || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_BUFFERING
                    || musicServiceConnection.currentPlayingSong.value != null
                ) {
                    val songIcon =
                        musicServiceConnection.currentPlayingSong.value?.description?.iconUri.toString()
                    val title =
                        musicServiceConnection.currentPlayingSong.value?.description?.title.toString()
                    PlayerBottomBar(
                        songIcon = songIcon, title = title,
                        onSkipNextPressed = { musicServiceConnection.transportControls.skipToNext() },
                        musicServiceConnection = musicServiceConnection
                    )
                }
            }
        }
    )
}
