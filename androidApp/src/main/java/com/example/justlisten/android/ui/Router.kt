package com.example.justlisten.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.util.fastForEach
import coil.annotation.ExperimentalCoilApi
import com.example.justlisten.Navigation
import com.example.justlisten.android.exoplayer.MusicServiceConnection

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun Navigation.Router(musicServiceConnection: MusicServiceConnection) {
    val screenUIisStateHolder = rememberSaveableStateHolder()

    OnePane(screenUIisStateHolder, musicServiceConnection)

    screenStatesToRemove.fastForEach {
        screenUIisStateHolder.removeState(it.URI)
    }

    if (!only1ScreenInBackstack) {
        BackHandler { // catching the back button to update the DKMPViewModel
            exitScreen()
        }
    }
}