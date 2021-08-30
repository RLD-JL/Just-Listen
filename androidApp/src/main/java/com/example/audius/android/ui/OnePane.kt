package com.example.audius.android.ui

import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.SaveableStateHolder
import com.example.audius.Navigation
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.bottombars.Level1BottomBar
import com.example.audius.android.ui.screenpicker.ScreenPicker

@Composable
fun Navigation.OnePane(
    saveableStateHolder: SaveableStateHolder,
    musicServiceConnection: MusicServiceConnection
) {
    Scaffold(
        bottomBar = {
            if (currentScreenIdentifier.screen.navigationLevel == 1) Level1BottomBar(
                currentScreenIdentifier
            )
        },
        content = {
            saveableStateHolder.SaveableStateProvider(currentScreenIdentifier.URI) {
                ScreenPicker(currentScreenIdentifier, musicServiceConnection)
            }
        }
    )
}
