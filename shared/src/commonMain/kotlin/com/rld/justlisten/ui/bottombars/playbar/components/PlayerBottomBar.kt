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

@Composable
fun PlayerBottomBar(
    uiState: PlayerUiState,
    layoutInfo: PlayerLayoutInfo,
    onAction: (PlayerAction) -> Unit,
    onUiEvent: (PlayerUiEvent) -> Unit
) {
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

    val currentFraction = layoutInfo.currentFraction
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

    // Ease the fraction for the background blend too
    val eased = FastOutSlowInEasing.transform(currentFraction)

    // Blend: collapsed = minibarBackground, expanded = dominant color (darkened slightly)
    val blendedBackground = Color(
        red   = lerp(minibarBackground.red,   animatedColor.red   * 0.7f, eased).coerceIn(0f, 1f),
        green = lerp(minibarBackground.green, animatedColor.green * 0.7f, eased).coerceIn(0f, 1f),
        blue  = lerp(minibarBackground.blue,  animatedColor.blue  * 0.7f, eased).coerceIn(0f, 1f),
        alpha = 1f
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(blendedBackground)
            .noRippleClickable { onUiEvent(PlayerUiEvent.CloseSheet) }
    ) {
        val constraints = this@BoxWithConstraints

        // ── 1. Progress bar (minibar only) ──────────────────────────────────
        val progress = if ((playbackState.currentMedia?.duration ?: 0L) > 0L)
            playbackState.currentPosition.toFloat() /
                    playbackState.currentMedia!!.duration.toFloat()
        else 0f

        if (!progress.isNaN() && currentFraction < 0.99f) {
            LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.5.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = 63.5.dp)
                                .graphicsLayer {
                                    alpha = (1f - currentFraction * 4f).coerceIn(0f, 1f)
                                },
            color = animatedColor.copy(alpha = 0.85f),
            trackColor = ProgressIndicatorDefaults.linearTrackColor,
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        }

        // ── 2. Album art + minimized controls ───────────────────────────────
        PlayBarSwipeActions(
            songIcon = songIcon,
            highResIcon = artworkUrl,
            currentFraction = currentFraction,
            constraints = constraints,
            title = title,
            onSkipNextPressed = { onAction(PlayerAction.SkipNext) },
            onSkipPreviousPressed = { onAction(PlayerAction.SkipPrevious) },
            painterLoaded = { onUiEvent(PlayerUiEvent.PainterLoaded(it)) },
            onFavoritePressed = { songId, songTitle, songUser, songIconList, isFav ->
                onAction(PlayerAction.ToggleFavorite(songId, songTitle, songUser, songIconList, isFav))
            },
            newDominantColor = { color ->
                val extracted = Color(color)
                targetColor = androidx.compose.ui.graphics.lerp(extracted, primaryThemeColor, 0.3f)
                onUiEvent(PlayerUiEvent.DominantColorExtracted(color))
            },
            playBarMinimizedClicked = { onUiEvent(PlayerUiEvent.Expand) }
        )

        // ── 3. Top section: collapse arrow + more (expanded only) ───────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            PlayBarTopSection(
                currentFraction = currentFraction,
                onCollapsedClicked = { onUiEvent(PlayerUiEvent.Collapse) }
            )
        }

        // ── 4. Playback controls + seek bar (fade in after 40% expanded) ────
        AnimatedVisibility(
            visible = currentFraction > 0.4f,
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
            visible = currentFraction > 0.85f,
            enter = fadeIn(tween(180)) + slideInVertically(
                tween(220), initialOffsetY = { it / 2 }
            ),
            exit = fadeOut(tween(130)) + slideOutVertically(
                tween(160), targetOffsetY = { it / 2 }
            )
        ) {
            PlayerBottomTabs(
                currentFraction = currentFraction,
                maxHeight = constraints.maxHeight,
                bottomPadding = bottomPadding
            )
        }
    }
}