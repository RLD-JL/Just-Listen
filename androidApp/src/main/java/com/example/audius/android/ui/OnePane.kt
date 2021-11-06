package com.example.audius.android.ui

import android.media.session.PlaybackState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.audius.Navigation
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.bottombars.Level1BottomBar
import com.example.audius.android.ui.bottombars.PlayerBottomBar
import com.example.audius.android.ui.screenpicker.ScreenPicker
import com.example.audius.android.ui.utils.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

enum class SheetState { Open, Closed }

val LazyListState.isScrolled: Boolean
    get() = firstVisibleItemIndex > 0 || firstVisibleItemScrollOffset > 0

@ExperimentalMaterialApi
@Composable
fun Navigation.OnePane(
    saveableStateHolder: SaveableStateHolder,
    musicServiceConnection: MusicServiceConnection
) {
    val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

    Scaffold(
        bottomBar = {
            if (currentScreenIdentifier.screen.navigationLevel == 1) {
                Level1BottomBar(currentScreenIdentifier)
            }
        },
        content = {
            val bottomBarPadding = it.calculateBottomPadding()
            Surface {
                BottomSheetScaffold(modifier = if (musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PLAYING
                    || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PAUSED
                    || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_SKIPPING_TO_NEXT
                    || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_BUFFERING
                    || musicServiceConnection.currentPlayingSong.value != null) {
                        Modifier.padding(bottom = bottomBarPadding)} else Modifier,
                    sheetContent = {
                    PlayerBarSheet( onSkipNextPressed = { musicServiceConnection.transportControls.skipToNext() },
                        musicServiceConnection = musicServiceConnection)
                }, content = {
                    Column(modifier = Modifier.padding(bottom = bottomBarPadding )) {
                        saveableStateHolder.SaveableStateProvider(currentScreenIdentifier.URI) {
                            ScreenPicker(currentScreenIdentifier, musicServiceConnection)
                        }
                    }
                })

            }
        })

    /*
    BoxWithConstraints {
        val sheetState = rememberSwipeableState(SheetState.Closed)
        val fabSize = with(LocalDensity.current) { 56.dp.toPx() }
        val dragRange = constraints.maxHeight.toFloat()
        val scope = rememberCoroutineScope()

            Scaffold(
                bottomBar = {
                    if (currentScreenIdentifier.screen.navigationLevel == 1) {
                        Level1BottomBar(currentScreenIdentifier)
                    }
                },
                content = {

                    Box(
                        Modifier.swipeable(
                            state = sheetState,
                            anchors = mapOf(
                                0f to SheetState.Closed,
                                -dragRange  to SheetState.Open
                            ),
                            thresholds = { _, _ -> FractionalThreshold(0.5f) },
                            orientation = Orientation.Vertical,
                        velocityThreshold = 0.dp)
                    ) {
                        val openFraction = if (sheetState.offset.value.isNaN()) {
                            0f
                        } else {
                            -sheetState.offset.value / dragRange
                        }.coerceIn(0f, 1f)

                        Surface(modifier = Modifier
                            .fillMaxSize()) {
                            Column(modifier = Modifier) {
                                saveableStateHolder.SaveableStateProvider(currentScreenIdentifier.URI) {
                                    ScreenPicker(currentScreenIdentifier, musicServiceConnection)
                                }
                            }
                        }

                        PlayerBarSheet(
                            openFraction = openFraction,
                            height = this@BoxWithConstraints.constraints.maxHeight.toFloat(),
                            onSkipNextPressed = { musicServiceConnection.transportControls.skipToNext() },
                            musicServiceConnection = musicServiceConnection
                        ) { state ->
                            scope.launch {
                                sheetState.animateTo(state)
                            }
                        }
                    }
                })


    }

     */
}

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

