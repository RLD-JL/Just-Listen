package com.example.audius.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.viewmodel.AudiusViewModel

@Composable
fun MainComposable(model: AudiusViewModel, musicServiceConnection: MusicServiceConnection) {
    val appState by model.stateFlow.collectAsState()
    val audiusNav = appState.getNavigation(model = model)
    audiusNav.Router(musicServiceConnection)
}