package com.example.audius.android.ui

import android.media.session.PlaybackState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.example.audius.Navigation
import com.example.audius.android.R
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.bottombars.Level1BottomBar
import com.example.audius.android.ui.bottombars.PlayerBottomBar
import com.example.audius.android.ui.bottombars.sheetcontent.SheetCollapsed
import com.example.audius.android.ui.bottombars.sheetcontent.SheetContent
import com.example.audius.android.ui.bottombars.sheetcontent.SheetExpanded
import com.example.audius.android.ui.extensions.currentFraction
import com.example.audius.android.ui.screenpicker.ScreenPicker
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

    val scope = rememberCoroutineScope()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )

    val sheetToggle: () -> Unit = {
        scope.launch {
            if (scaffoldState.bottomSheetState.isCollapsed) {
                scaffoldState.bottomSheetState.expand()
            } else {
                scaffoldState.bottomSheetState.collapse()
            }
        }
    }

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
                        SheetContent {
                            SheetExpanded {
                                SongDetails()
                            }
                            SheetCollapsed(
                                isCollapsed = scaffoldState.bottomSheetState.isCollapsed,
                                currentFraction = scaffoldState.currentFraction,
                                onSheetClick = sheetToggle
                            ) {
                                PlayerBarSheet(
                                    onSkipNextPressed = { musicServiceConnection.transportControls.skipToNext() },
                                    musicServiceConnection = musicServiceConnection
                                )
                            }
                        }
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

@Composable
fun SongDetails() {
    Column() {
        Image(
            painter = painterResource(id = R.drawable.camelia),
            modifier = Modifier.size(350.dp),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        Text(text = "yolo")

        Text(text = "yolo")
    }
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

