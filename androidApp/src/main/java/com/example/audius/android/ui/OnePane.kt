package com.example.audius.android.ui

import android.media.session.PlaybackState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.example.audius.Navigation
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.bottombars.Level1BottomBar
import com.example.audius.android.ui.bottombars.PlayerBottomBar
import com.example.audius.android.ui.screenpicker.ScreenPicker

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun Navigation.OnePane(
    saveableStateHolder: SaveableStateHolder,
    musicServiceConnection: MusicServiceConnection
) {
    val shouldHavePlayBar =
        musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PLAYING
                || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PAUSED
                || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_SKIPPING_TO_NEXT
                || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_BUFFERING
                || musicServiceConnection.currentPlayingSong.value != null

    Scaffold(
        bottomBar = {
            if (currentScreenIdentifier.screen.navigationLevel == 1) {
                Level1BottomBar(currentScreenIdentifier)
            }
        },
        content = {
            val bottomBarPadding = it.calculateBottomPadding()
                BottomSheetScaffold(
                    sheetContent = {
                        PlayerBarSheet(
                            onSkipNextPressed = { musicServiceConnection.transportControls.skipToNext() },
                            musicServiceConnection = musicServiceConnection
                        )
                    }, content = {
                        Column(
                            modifier = if (shouldHavePlayBar) Modifier.padding(bottom = bottomBarPadding + 55.dp) else
                                Modifier.padding(bottom = bottomBarPadding)
                        ) {
                            saveableStateHolder.SaveableStateProvider(currentScreenIdentifier.URI) {
                                ScreenPicker(currentScreenIdentifier, musicServiceConnection)
                            }
                        }
                    }, sheetPeekHeight = if (shouldHavePlayBar) {
                        bottomBarPadding + 65.dp
                    } else bottomBarPadding - 50.dp
                )
        })
}

@ExperimentalCoilApi
@Composable
fun PlayerBarSheet(
    onSkipNextPressed: () -> Unit,
    musicServiceConnection: MusicServiceConnection,
) {
    PlayerBottomBar(
        onSkipNextPressed = onSkipNextPressed,
        musicServiceConnection = musicServiceConnection
    )

}

