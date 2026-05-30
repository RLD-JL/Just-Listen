package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.extensions.noRippleClickable
import com.rld.justlisten.ui.theme.modifiers.verticalGradientBackgroundColor
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.ui.utils.lerp

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

    var gradientColor by remember {
        mutableStateOf(0xFF1C1C1E.toInt()) // Sleek dark grey/black background fallback
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .verticalGradientBackgroundColor(gradientColor)
            .noRippleClickable { onBackgroundClicked() }
    ) {
        val constraints = this@BoxWithConstraints

        // 1. Minimized Progress Indicator
        val progress = if ((playbackState.currentMedia?.duration ?: 0L) > 0) {
            playbackState.currentPosition.toFloat() / playbackState.currentMedia!!.duration.toFloat()
        } else 0f

        if (!progress.isNaN() && currentFraction < 0.99f) {
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .align(Alignment.TopCenter)
                    .graphicsLayer {
                        alpha = (1f - currentFraction * 10f).coerceIn(0f, 1f)
                        translationY = 64.dp.toPx() // Float exactly at the bottom of the collapsed minibar
                    }
            )
        }

        // 2. PlayBarSwipeActions (Unified image and minimized controls, completely flat layout)
        PlayBarSwipeActions(
            songIcon,
            artworkUrl,
            currentFraction,
            constraints,
            title,
            musicPlayer,
            onSkipNextPressed,
            painterLoaded,
            onFavoritePressed,
            newDominantColor = { color ->
                gradientColor = color
                newDominantColor(color)
            },
            playBarMinimizedClicked = playBarMinimizedClicked
        )

        // 3. PlayBarTopSection (Expandable collapse/more icons at the top)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        ) {
            PlayBarTopSection(currentFraction, onCollapsedClicked, onMoreClicked)
        }

        // 4. PlayBarActionsMaximized (Fades and slides up in the bottom half of the screen)
        if (currentFraction > 0.05f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 56.dp)
                    .graphicsLayer {
                        alpha = lerp(0f, 1f, 0.4f, 1f, currentFraction)
                        translationY = lerp(120f, 0f, 0.4f, 1f, currentFraction)
                    }
            ) {
                PlayBarActionsMaximized(
                    bottomPadding,
                    currentFraction,
                    musicPlayer,
                    title,
                    onSkipNextPressed,
                    onFavoritePressed,
                    onSaveClicked = onSaveClicked
                )
            }
        }

        // 5. PlayerBottomTabs (Slides in at the very bottom when fully extended)
        if (currentFraction > 0.8f) {
            PlayerBottomTabs(
                currentFraction = currentFraction,
                musicPlayer = musicPlayer,
                maxHeight = constraints.maxHeight,
                bottomPadding = bottomPadding
            )
        }
    }
}
