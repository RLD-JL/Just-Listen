package com.rld.justlisten.ui.bottombars.playbar

import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private enum class CommentsSheetValue {
    Expanded,
    Hidden,
}

/**
 * In-tree comments sheet using the same anchored-drag handoff as the player tabs.
 * Keeping it out of a modal dialog prevents iOS from cancelling an active drag
 * when a nested comments list reaches its scroll boundary.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun BoxScope.DraggableCommentsSheet(
    onDismissRequest: () -> Unit,
    content: @Composable (dismiss: () -> Unit) -> Unit,
) {
    val currentOnDismissRequest by rememberUpdatedState(onDismissRequest)
    val coroutineScope = rememberCoroutineScope()
    var sheetHeightPx by remember { mutableFloatStateOf(0f) }
    val sheetState = remember {
        AnchoredDraggableState(initialValue = CommentsSheetValue.Expanded)
    }

    val anchors = remember(sheetHeightPx) {
        DraggableAnchors {
            CommentsSheetValue.Expanded at 0f
            if (sheetHeightPx > 0f) CommentsSheetValue.Hidden at sheetHeightPx
        }
    }
    LaunchedEffect(anchors) { sheetState.updateAnchors(anchors) }

    val baseFlingBehavior = AnchoredDraggableDefaults.flingBehavior(
        state = sheetState,
        positionalThreshold = { distance: Float -> distance * 0.3f },
        animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
    )
    val dismissingFlingBehavior = remember(baseFlingBehavior, sheetState) {
        object : FlingBehavior {
            override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
                val remaining = with(baseFlingBehavior) { performFling(initialVelocity) }
                if (sheetState.currentValue == CommentsSheetValue.Hidden) {
                    currentOnDismissRequest()
                }
                return remaining
            }
        }
    }

    val nestedScrollConnection = remember(sheetState, dismissingFlingBehavior) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val currentOffset = sheetState.offset
                if (available.y < 0f && !currentOffset.isNaN() && currentOffset > 0f) {
                    return Offset(0f, sheetState.dispatchRawDelta(available.y))
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                if (available.y > 0f) {
                    return Offset(0f, sheetState.dispatchRawDelta(available.y))
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val currentOffset = sheetState.offset
                if (!currentOffset.isNaN() && currentOffset > 0f) {
                    val scrollScope = object : ScrollScope {
                        override fun scrollBy(pixels: Float): Float =
                            sheetState.dispatchRawDelta(pixels)
                    }
                    with(dismissingFlingBehavior) {
                        scrollScope.performFling(available.y)
                    }
                    return available
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity,
            ): Velocity {
                val scrollScope = object : ScrollScope {
                    override fun scrollBy(pixels: Float): Float =
                        sheetState.dispatchRawDelta(pixels)
                }
                with(dismissingFlingBehavior) {
                    scrollScope.performFling(available.y)
                }
                return available
            }
        }
    }

    val dismiss: () -> Unit = {
        coroutineScope.launch {
            sheetState.animateTo(CommentsSheetValue.Hidden)
            currentOnDismissRequest()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = dismiss),
    )

    Surface(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .fillMaxHeight(0.95f)
            .onSizeChanged { sheetHeightPx = it.height.toFloat() }
            .offset {
                IntOffset(
                    x = 0,
                    y = sheetState.offset.takeUnless(Float::isNaN)?.roundToInt() ?: 0,
                )
            }
            .nestedScroll(nestedScrollConnection)
            .anchoredDraggable(
                state = sheetState,
                orientation = Orientation.Vertical,
                flingBehavior = dismissingFlingBehavior,
            ),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.background,
    ) {
        androidx.compose.foundation.layout.Column(Modifier.fillMaxSize()) {
            BottomSheetDefaults.DragHandle(
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            content(dismiss)
        }
    }
}
