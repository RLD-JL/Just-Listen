package com.example.audius.android.ui.theme

import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.SaveableStateHolder
import com.example.audius.Navigation
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.Level1BottomBar

@Composable
fun Navigation.OnePane(saveableStateHolder: SaveableStateHolder, musicServiceConnection: MusicServiceConnection) {
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
