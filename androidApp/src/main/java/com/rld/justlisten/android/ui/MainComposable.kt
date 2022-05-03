package com.rld.justlisten.android.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.rld.justlisten.android.exoplayer.MusicServiceConnection
import com.rld.justlisten.viewmodel.JustListenViewModel

@ExperimentalMaterialApi
@Composable
fun MainComposable(
    model: JustListenViewModel,
    musicServiceConnection: MusicServiceConnection,
    settingsUpdated: () -> Unit
) {
    val appState by model.stateFlow.collectAsState()
    val justlistenNav = appState.getNavigation(model = model)
    justlistenNav.Router(musicServiceConnection, settingsUpdated = settingsUpdated)
}