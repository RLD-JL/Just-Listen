package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableDefaults
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.ui.bottombars.playbar.components.upnext.UpNextQueueView
import com.rld.justlisten.viewmodel.player.PlayerUiState
import com.rld.justlisten.ui.actions.PlayerAction
import kotlinx.coroutines.launch

enum class TabsSheetState {
    COLLAPSED,
    EXPANDED
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerBottomTabs(
    maxHeight: Dp,
    bottomPadding: Dp,
    uiState: PlayerUiState,
    onAction: (PlayerAction) -> Unit
) {
    val musicPlayer = LocalMusicPlayer.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("UP NEXT", "LYRICS", "RELATED")
    val playlist by musicPlayer.currentPlaylist.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val density = LocalDensity.current
    val bottomSafeArea = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
    val startAnchor = with(density) { 120.dp.toPx() }
    val endAnchor = with(density) { (maxHeight - 56.dp - bottomSafeArea).toPx() }
    val decayAnimationSpec = rememberSplineBasedDecay<Float>()

    val anchoredDraggableState = remember(endAnchor) {
        AnchoredDraggableState(
            initialValue = TabsSheetState.COLLAPSED,
            anchors = DraggableAnchors {
                TabsSheetState.EXPANDED at startAnchor
                TabsSheetState.COLLAPSED at endAnchor
            },
            positionalThreshold = { distance: Float -> distance * 0.3f },
            velocityThreshold = { with(density) { 125.dp.toPx() } },
            snapAnimationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
            decayAnimationSpec = decayAnimationSpec
        )
    }

    val nestedScrollConnection = remember(anchoredDraggableState, startAnchor, endAnchor) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                val offset = anchoredDraggableState.offset
                if (!offset.isNaN()) {
                    val isIntermediate = offset > startAnchor && offset < endAnchor
                    if (isIntermediate || (delta < 0f && offset > startAnchor)) {
                        val consumed = anchoredDraggableState.dispatchRawDelta(delta)
                        return Offset(0f, consumed)
                    }
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                if (delta > 0f) {
                    val consumedByDraggable = anchoredDraggableState.dispatchRawDelta(delta)
                    return Offset(0f, consumedByDraggable)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val offset = anchoredDraggableState.offset
                if (!offset.isNaN() && offset > startAnchor && offset < endAnchor) {
                    anchoredDraggableState.settle(available.y)
                    return available
                }
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                anchoredDraggableState.settle(available.y)
                return available
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(maxHeight - 120.dp - bottomSafeArea)
            .offset {
                IntOffset(
                    0,
                    anchoredDraggableState.offset.toInt()
                )
            }
            .nestedScroll(nestedScrollConnection)
            .anchoredDraggable(
                state = anchoredDraggableState,
                orientation = Orientation.Vertical,
                flingBehavior = AnchoredDraggableDefaults.flingBehavior(
                    state = anchoredDraggableState,
                    positionalThreshold = { distance: Float -> distance * 0.3f },
                    animationSpec = spring(stiffness = 300f, dampingRatio = 0.8f)
                )
            )
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(Color.Black.copy(alpha = 0.95f))
            .clickable(enabled = false) {}
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Drag handle header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clickable {
                        coroutineScope.launch {
                            val target =
                                if (anchoredDraggableState.currentValue == TabsSheetState.COLLAPSED)
                                    TabsSheetState.EXPANDED
                                else
                                    TabsSheetState.COLLAPSED
                            anchoredDraggableState.animateTo(target)
                        }
                    }
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(Color.White.copy(alpha = 0.3f))
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        tabs.forEachIndexed { index, tab ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedTab = index }
                            ) {
                                Text(
                                    text = tab,
                                    color = if (selectedTab == index) Color.White else Color.White.copy(
                                        alpha = 0.6f
                                    ),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

            // Tab Content
            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> { // UP NEXT
                        UpNextQueueView(
                            playlist = playlist,
                            uiState = uiState,
                            onAction = onAction
                        )
                    }

                    1 -> { // LYRICS
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No lyrics found.", color = Color.White)
                        }
                    }

                    2 -> { // RELATED
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No related tracks.", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}