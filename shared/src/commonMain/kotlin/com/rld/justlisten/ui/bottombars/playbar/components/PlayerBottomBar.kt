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
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.extensions.noRippleClickable
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.ui.utils.lerp

// Dark base colour used when collapsed or no artwork colour available
private val MinibarBackground = Color(0xFF1C1C1E)

@Composable
fun PlayerBottomBar(
    bottomPadding: Dp,
    currentFraction: Float,
    isExtended: Boolean,
    songIcon: String,
    artworkUrl: String,
    title: String,
    musicPlayer: MusicPlayer,
    onSkipNextPressed: () -> Unit,
    onCollapsedClicked: () -> Unit,
    onMoreClicked: () -> Unit,
    onBackgroundClicked: () -> Unit,
    painterLoaded: (Painter) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    onSaveClicked: () -> Unit,
    newDominantColor: (Int) -> Unit,
    playBarMinimizedClicked: () -> Unit
) {
    val playbackState by musicPlayer.playbackState.collectAsState()

    val primaryThemeColor = androidx.compose.material3.MaterialTheme.colorScheme.primary

    // Dominant color extracted from artwork
    var targetColor by remember { mutableStateOf(MinibarBackground) }
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 800, easing = LinearEasing),
        label = "dominantColor"
    )

    // Ease the fraction for the background blend too
    val eased = FastOutSlowInEasing.transform(currentFraction)

    // Blend: collapsed = MinibarBackground, expanded = dominant color (darkened slightly)
    val blendedBackground = Color(
        red   = lerp(MinibarBackground.red,   animatedColor.red   * 0.7f, eased).coerceIn(0f, 1f),
        green = lerp(MinibarBackground.green, animatedColor.green * 0.7f, eased).coerceIn(0f, 1f),
        blue  = lerp(MinibarBackground.blue,  animatedColor.blue  * 0.7f, eased).coerceIn(0f, 1f),
        alpha = 1f
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(blendedBackground)
            .noRippleClickable { onBackgroundClicked() }
    ) {
        val constraints = this@BoxWithConstraints

        // ── 1. Progress bar (minibar only) ──────────────────────────────────
        val progress = if ((playbackState.currentMedia?.duration ?: 0L) > 0L)
            playbackState.currentPosition.toFloat() /
                    playbackState.currentMedia!!.duration.toFloat()
        else 0f

        if (!progress.isNaN() && currentFraction < 0.99f) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        alpha = (1f - currentFraction * 4f).coerceIn(0f, 1f)
                    },
                color = animatedColor.copy(alpha = 0.85f),
                backgroundColor = Color.White.copy(alpha = 0.15f)
            )
        }

        // ── 2. Album art + minimized controls ───────────────────────────────
        PlayBarSwipeActions(
            songIcon = songIcon,
            highResIcon = artworkUrl,
            currentFraction = currentFraction,
            constraints = constraints,
            title = title,
            musicPlayer = musicPlayer,
            onSkipNextPressed = onSkipNextPressed,
            painterLoaded = painterLoaded,
            onFavoritePressed = onFavoritePressed,
            newDominantColor = { color ->
                val extracted = Color(color)
                targetColor = androidx.compose.ui.graphics.lerp(extracted, primaryThemeColor, 0.6f)
                newDominantColor(color)
            },
            playBarMinimizedClicked = playBarMinimizedClicked
        )

        // ── 3. Top section: collapse arrow + more (expanded only) ───────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            PlayBarTopSection(currentFraction, onCollapsedClicked, onMoreClicked)
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
                .padding(bottom = 56.dp)
        ) {
            PlayBarActionsMaximized(
                bottomPadding = bottomPadding,
                currentFraction = currentFraction,
                musicPlayer = musicPlayer,
                title = title,
                onSkipNextPressed = onSkipNextPressed,
                onFavoritePressed = onFavoritePressed,
                onSaveClicked = onSaveClicked
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
                musicPlayer = musicPlayer,
                maxHeight = constraints.maxHeight,
                bottomPadding = bottomPadding
            )
        }
    }
}