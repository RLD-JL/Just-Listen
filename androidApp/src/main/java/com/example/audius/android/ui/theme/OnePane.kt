package com.example.audius.android.ui.theme

import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.SaveableStateHolder
import com.example.audius.Navigation
import com.example.audius.android.ui.Level1BottomBar

@Composable
fun Navigation.OnePane(saveableStateHolder: SaveableStateHolder) {
    Scaffold(
        content = {
            saveableStateHolder.SaveableStateProvider(currentScreenIdentifier.URI) {
                ScreenPicker(currentScreenIdentifier)
            }
        },
        bottomBar = {
            if (currentScreenIdentifier.screen.navigationLevel == 1) Level1BottomBar(
                currentScreenIdentifier
            )
        }
    )
}
