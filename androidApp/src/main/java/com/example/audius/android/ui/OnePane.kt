package com.example.audius.android.ui

import android.media.session.PlaybackState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.example.audius.Navigation
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.bottombars.Level1BottomBar
import com.example.audius.android.ui.bottombars.playbar.PlayerBottomBar
import com.example.audius.android.ui.extensions.fraction
import com.example.audius.android.ui.screenpicker.ScreenPicker
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

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

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )

    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        bottomBar = {
            if (currentScreenIdentifier.screen.navigationLevel == 1) {
                Level1BottomBar(currentScreenIdentifier)
            }
        },
        content = {
            val bottomBarPadding = it.calculateBottomPadding()
                BottomSheetScaffold(
                    modifier = Modifier.fillMaxSize(),
                    scaffoldState = scaffoldState,
                    sheetContent = {
                                PlayerBarSheet(
                                    onCollapsedClicked = {coroutineScope.launch {scaffoldState.bottomSheetState.collapse()}},
                                    bottomPadding = bottomBarPadding,
                                    currentFraction = scaffoldState.fraction,
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

@OptIn(InternalCoroutinesApi::class)
@ExperimentalCoilApi
@Composable
fun PlayerBarSheet(
    bottomPadding: Dp,
    currentFraction: Float,
    onSkipNextPressed: () -> Unit,
    musicServiceConnection: MusicServiceConnection,
    onCollapsedClicked: () -> Unit,
) {
    PlayerBottomBar(
        bottomPadding = bottomPadding,
        currentFraction = currentFraction,
        onSkipNextPressed = onSkipNextPressed,
        musicServiceConnection = musicServiceConnection,
        onCollapsedClicked = onCollapsedClicked,
    )

}

