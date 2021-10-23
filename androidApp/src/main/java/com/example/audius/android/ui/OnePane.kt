package com.example.audius.android.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
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
    BoxWithConstraints {
        val sheetState = rememberSwipeableState(SheetState.Closed)
        val fabSize = with(LocalDensity.current) { 56.dp.toPx() }
        val dragRange = constraints.maxHeight.toFloat()
        val scope = rememberCoroutineScope()
        val wishlisted = (1..3).map { "Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it " +
                "Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it " +
                "Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it " +
                "Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it " +
                "Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it Wishlisted Book $it " }

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

                        val bottomBarPadding = it.calculateBottomPadding()
                        Surface(modifier = Modifier.fillMaxSize().padding(bottom = bottomBarPadding)) {
                            LazyColumn {
                                item{ saveableStateHolder.SaveableStateProvider(currentScreenIdentifier.URI) {
                                    ScreenPicker(currentScreenIdentifier, musicServiceConnection)
                                }}
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
}

@Composable
fun PlayerBarSheet(
    openFraction: Float,
    height: Float,
    onSkipNextPressed: () -> Unit,
    musicServiceConnection: MusicServiceConnection,
    updateSheet: (SheetState) -> Unit
) {

    // Use the fraction that the sheet is open to drive the transformation from FAB -> Sheet
    val fabSize = with(LocalDensity.current) { 56.dp.toPx() }
    val offsetY = lerp(height - (2*fabSize) , 0f, openFraction)

    Surface(
        modifier = Modifier.graphicsLayer {
            translationY = offsetY
        }
    ) {
        PlayerBottomBar(
            openFraction,
            updateSheet,
            onSkipNextPressed = onSkipNextPressed,
            musicServiceConnection = musicServiceConnection
        )
    }
}

