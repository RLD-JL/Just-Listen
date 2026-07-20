package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.layout
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.rld.justlisten.ui.extensions.noRippleClickable
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.ui.utils.lerp
import com.rld.justlisten.viewmodel.player.PlayerUiState
import com.rld.justlisten.ui.actions.PlayerAction
import com.rld.justlisten.ui.bottombars.playbar.PlayerLayoutInfo
import com.rld.justlisten.ui.bottombars.playbar.PlayerUiEvent
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.media.MusicPlayer

@Composable
fun PlayerBottomBar(
    uiState: PlayerUiState,
    layoutInfo: PlayerLayoutInfo,
    onAction: (PlayerAction) -> Unit,
    onUiEvent: (PlayerUiEvent) -> Unit
) {
    val musicPlayer = LocalMusicPlayer.current
    val playbackState = uiState.playbackState ?: com.rld.justlisten.media.PlaybackState(
        status = com.rld.justlisten.media.PlaybackStatus.IDLE,
        currentPosition = 0
    )

    if (uiState.showConnectPrompt) {
        com.rld.justlisten.ui.artistprofile.components.ConnectPromptDialog(
            onDismissRequest = { onAction(PlayerAction.DismissConnectPrompt) },
            onConnectClick = { onAction(PlayerAction.ConnectAudiusPressed) }
        )
    }

    val currentMedia = playbackState.currentMedia
    val songIcon = currentMedia?.lowResArtworkUrl ?: currentMedia?.artworkUrl ?: ""
    val artworkUrl = currentMedia?.artworkUrl ?: ""
    val title = currentMedia?.title ?: ""

    val currentFractionProvider = layoutInfo.currentFractionProvider
    val bottomPadding = layoutInfo.bottomPadding
    val bottomSafeArea = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()

    val minibarBackground = androidx.compose.material3.MaterialTheme.colorScheme.background
    val primaryThemeColor = androidx.compose.material3.MaterialTheme.colorScheme.primary

    // Dominant color extracted from artwork
    var targetColor by remember(minibarBackground) { mutableStateOf(minibarBackground) }
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "dominantColor"
    )

    // Derived states to prevent recomposing PlayerBottomBar on every pixel
    val isMaximizedControlsVisible by remember(layoutInfo) {
        derivedStateOf { currentFractionProvider() > 0.4f }
    }
    val isBottomTabsVisible by remember(layoutInfo) {
        derivedStateOf { currentFractionProvider() > 0.85f }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .drawBehind {
                val fraction = currentFractionProvider()
                val eased = FastOutSlowInEasing.transform(fraction)
                val blendedBackground = Color(
                    red   = lerp(minibarBackground.red,   animatedColor.red   * 0.7f, eased).coerceIn(0f, 1f),
                    green = lerp(minibarBackground.green, animatedColor.green * 0.7f, eased).coerceIn(0f, 1f),
                    blue  = lerp(minibarBackground.blue,  animatedColor.blue  * 0.7f, eased).coerceIn(0f, 1f),
                    alpha = 1f
                )

                val cornerRadiusPx = (16f * (1f - eased)).dp.toPx()
                val horizontalInsetPx = (16f * (1f - eased)).dp.toPx()

                drawRoundRect(
                    color = blendedBackground,
                    topLeft = Offset(x = horizontalInsetPx, y = 0f),
                    size = Size(
                        width = size.width - (horizontalInsetPx * 2),
                        height = size.height
                    ),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
                )
            }
            .noRippleClickable { onUiEvent(PlayerUiEvent.CloseSheet) }
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .layout { measurable, constraints ->
                    val fraction = currentFractionProvider()
                    val eased = FastOutSlowInEasing.transform(fraction)
                    val horizontalPaddingPx = (16f * (1f - eased)).dp.toPx().toInt()

                    val minWidth = (constraints.minWidth - horizontalPaddingPx * 2).coerceAtLeast(0)
                    val maxWidth = (constraints.maxWidth - horizontalPaddingPx * 2).coerceAtLeast(0)

                    val placeable = measurable.measure(
                        androidx.compose.ui.unit.Constraints(
                            minWidth = minWidth,
                            maxWidth = maxWidth,
                            minHeight = constraints.minHeight,
                            maxHeight = constraints.maxHeight
                        )
                    )

                    layout(constraints.maxWidth, constraints.maxHeight) {
                        placeable.place(horizontalPaddingPx, 0)
                    }
                }
                .graphicsLayer {
                    val fraction = currentFractionProvider()
                    val eased = FastOutSlowInEasing.transform(fraction)
                    val cornerRadiusPx = (16f * (1f - eased)).dp.toPx()
                    shape = RoundedCornerShape(cornerRadiusPx)
                    clip = true
                }
        ) playerBox@ {
            val playerConstraints = this@playerBox

            // ── 1. Progress bar (minibar only) ──────────────────────────────────
            MiniProgressBar(
                musicPlayer = musicPlayer,
                currentFractionProvider = currentFractionProvider,
                animatedColor = animatedColor
            )

            // ── 2. Album art + minimized controls ───────────────────────────────
            PlayBarSwipeActions(
                songIcon = songIcon,
                highResIcon = artworkUrl,
                currentFractionProvider = currentFractionProvider,
                constraints = playerConstraints,
                title = title,
                onSkipNextPressed = { onAction(PlayerAction.SkipNext) },
                onSkipPreviousPressed = { onAction(PlayerAction.SkipPrevious) },
                painterLoaded = { onUiEvent(PlayerUiEvent.PainterLoaded(it)) },
                newDominantColor = { color ->
                    val extracted = Color(color)
                    targetColor = androidx.compose.ui.graphics.lerp(extracted, primaryThemeColor, 0.3f)
                    onUiEvent(PlayerUiEvent.DominantColorExtracted(color))
                },
                playBarMinimizedClicked = { onUiEvent(PlayerUiEvent.Expand) },
                playbackState = playbackState
            )

            // ── 3. Top section: collapse arrow + more (expanded only) ───────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                PlayBarTopSection(
                    currentFractionProvider = currentFractionProvider,
                    onCollapsedClicked = { onUiEvent(PlayerUiEvent.Collapse) }
                )
            }

            // ── 4. Playback controls + seek bar (fade in after 40% expanded) ────
            AnimatedVisibility(
                visible = isMaximizedControlsVisible,
                enter = fadeIn(tween(220)) + slideInVertically(
                    tween(280), initialOffsetY = { it / 3 }
                ),
                exit = fadeOut(tween(160)) + slideOutVertically(
                    tween(200), targetOffsetY = { it / 3 }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 56.dp + bottomSafeArea)
            ) {
                PlayBarActionsMaximized(
                    uiState = uiState,
                    layoutInfo = layoutInfo,
                    onAction = onAction,
                    onUiEvent = onUiEvent
                )
            }

            // ── 5. Bottom tabs (UP NEXT / LYRICS / RELATED) ─────────────────────
            AnimatedVisibility(
                visible = isBottomTabsVisible,
                enter = fadeIn(tween(180)) + slideInVertically(
                    tween(220), initialOffsetY = { it / 2 }
                ),
                exit = fadeOut(tween(130)) + slideOutVertically(
                    tween(160), targetOffsetY = { it / 2 }
                )
            ) {
                PlayerBottomTabs(
                    maxHeight = playerConstraints.maxHeight,
                    bottomPadding = bottomPadding,
                    uiState = uiState,
                    onAction = onAction
                )
            }
        }
    }
}

@Composable
fun BoxScope.MiniProgressBar(
    musicPlayer: MusicPlayer,
    currentFractionProvider: () -> Float,
    animatedColor: Color
) {
    val playbackState by musicPlayer.playbackState.collectAsState()
    val duration = playbackState.currentMedia?.duration ?: 0L
    val progress = if (duration > 0L) {
        playbackState.currentPosition.toFloat() / duration.toFloat()
    } else 0f

    if (!progress.isNaN()) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .align(Alignment.TopCenter)
                .offset(y = 63.5.dp)
                .graphicsLayer {
                    val fraction = currentFractionProvider()
                    alpha = if (fraction >= 0.99f) 0f else (1f - fraction * 4f).coerceIn(0f, 1f)
                },
            color = animatedColor.copy(alpha = 0.85f),
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
        )
    }
}
