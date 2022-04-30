package com.example.justlisten.android.ui

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.justlisten.android.exoplayer.MusicServiceConnection
import com.example.justlisten.viewmodel.JustListenViewModel

@ExperimentalMaterialApi
@Composable
fun MainComposable(model: JustListenViewModel, musicServiceConnection: MusicServiceConnection) {
    val appState by model.stateFlow.collectAsState()
    val justlistenNav = appState.getNavigation(model = model)
    justlistenNav.Router(musicServiceConnection)
}