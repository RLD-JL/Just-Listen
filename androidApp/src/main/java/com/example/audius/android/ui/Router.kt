package com.example.audius.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import com.example.audius.Navigation
import com.example.audius.android.exoplayer.MusicServiceConnection

@Composable
fun Navigation.Router(musicServiceConnection: MusicServiceConnection) {
    val screenUIisStateHolder = rememberSaveableStateHolder()

    OnePane(screenUIisStateHolder, musicServiceConnection)

    screenStatesToRemove.forEach {
        screenUIisStateHolder.removeState(it.URI)
    }
}