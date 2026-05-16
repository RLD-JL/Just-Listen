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

@Composable
fun PlayerBottomBar(
    bottomPadding: Dp,
    currentFraction: Float,
    isExtended: Boolean,
    songIcon: String,
    title: String,
    musicPlayer: MusicPlayer,
    onSkipNextPressed: () -> Unit,
    onCollapsedClicked: () -> Unit,
    onMoreClicked: () -> Unit,
    onBackgroundClicked: () -> Unit,
    painterLoaded: (Painter) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    newDominantColor: (Int) -> Unit,
    playBarMinimizedClicked: () -> Unit
) {
    val playbackState by musicPlayer.playbackState.collectAsState()
    
    var gradientColor by remember {
        mutableStateOf(0xFF000000.toInt()) // Default black
    }

    BoxWithConstraints(
        modifier = if (isExtended || currentFraction > 0.001f) Modifier
            .fillMaxSize()
            .verticalGradientBackgroundColor(gradientColor)
            .noRippleClickable { onBackgroundClicked() } else Modifier.fillMaxWidth().height(65.dp).noRippleClickable { onBackgroundClicked() }
    ) {
        val constraints = this@BoxWithConstraints
        Column(Modifier.fillMaxSize()) {

            PlayBarTopSection(currentFraction, onCollapsedClicked, onMoreClicked)

            PlayBarSwipeActions(
                songIcon, currentFraction, constraints,
                title, musicPlayer, onSkipNextPressed, painterLoaded, onFavoritePressed,
                newDominantColor = { color ->
                    gradientColor = color
                    newDominantColor(color)
                },
                playBarMinimizedClicked = playBarMinimizedClicked
            )
            
            val progress = if ((playbackState.currentMedia?.duration ?: 0L) > 0) {
                playbackState.currentPosition.toFloat() / playbackState.currentMedia!!.duration.toFloat()
            } else 0f
            
            if (!progress.isNaN()) {
                LinearProgressIndicator(
                    progress = progress,
                    Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .graphicsLayer {
                            alpha = if (currentFraction > 0.001) 0f else 1f
                        }
                )
            }

            PlayBarActionsMaximized(
                bottomPadding,
                currentFraction,
                musicPlayer,
                title,
                onSkipNextPressed,
                constraints.maxWidth.value
            )
        }
    }
}
