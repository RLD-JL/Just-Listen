package com.example.audius.android.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.ui.Modifier
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
            if (currentScreenIdentifier.screen.navigationLevel == 1)  {
                Level1BottomBar(currentScreenIdentifier)
            }
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
                    PlayerBottomBar(
                        onSkipNextPressed = { musicServiceConnection.transportControls.skipToNext() },
                        musicServiceConnection = musicServiceConnection
                    )
                }
        }
    )
}
