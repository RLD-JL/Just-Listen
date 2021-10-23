package com.example.audius.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.example.audius.Navigation
import com.example.audius.android.exoplayer.MusicServiceConnection

@ExperimentalMaterialApi
@Composable
fun Navigation.Router(musicServiceConnection: MusicServiceConnection) {
    val screenUIisStateHolder = rememberSaveableStateHolder()

    OnePane(screenUIisStateHolder, musicServiceConnection)

    screenStatesToRemove.forEach {
        screenUIisStateHolder.removeState(it.URI)
    }

    if (!only1ScreenInBackstack) {
        BackHandler { // catching the back button to update the DKMPViewModel
            exitScreen()
        }
    }
}