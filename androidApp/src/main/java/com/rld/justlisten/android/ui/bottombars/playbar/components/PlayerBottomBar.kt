package com.rld.justlisten.android.ui.bottombars.playbar.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rld.justlisten.android.exoplayer.MusicService.Companion.curSongDuration
import com.rld.justlisten.android.exoplayer.MusicServiceConnection
import com.rld.justlisten.android.ui.extensions.noRippleClickable
import com.rld.justlisten.android.ui.theme.modifiers.verticalGradientBackgroundColor
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@Composable
fun PlayerBottomBar(
    bottomPadding: Dp,
    currentFraction: Float,
    isExtended: Boolean,
    songIcon: String,
    title: String,
    musicServiceConnection: MusicServiceConnection,
    onSkipNextPressed: () -> Unit,
    onCollapsedClicked: () -> Unit,
    onMoreClicked: () -> Unit,
    onBackgroundClicked: () -> Unit,
    painterLoaded: (Painter) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    newDominantColor: (Int) -> Unit,
    playBarMinimizedClicked: () -> Unit
) {
    val color = MaterialTheme.colors.background.toArgb()
    var gradientColor by remember {
        mutableStateOf(color)
    }

    BoxWithConstraints(
        modifier = if (isExtended) Modifier
            .verticalGradientBackgroundColor(gradientColor)
            .noRippleClickable { onBackgroundClicked() } else Modifier.noRippleClickable { onBackgroundClicked() }
    ) {
        val constraints = this@BoxWithConstraints
        Column(Modifier.fillMaxSize()) {

            PlayBarTopSection(currentFraction, onCollapsedClicked, onMoreClicked)

            PlayBarSwipeActions(
                songIcon, currentFraction, constraints,
                title, musicServiceConnection, onSkipNextPressed, painterLoaded, onFavoritePressed,
                newDominantColor = { color ->
                    gradientColor = color
                    newDominantColor(color)
                },
                playBarMinimizedClicked = playBarMinimizedClicked
            )
            LinearProgressIndicator(
                progress = musicServiceConnection.songDuration.value / curSongDuration.toFloat(),
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .graphicsLayer {
                        alpha = if (currentFraction > 0.001) 0f else 1f
                    }
            )
            PlayBarActionsMaximized(
                bottomPadding,
                currentFraction,
                musicServiceConnection,
                title,
                onSkipNextPressed,
                constraints.maxWidth.value
            )
        }
    }
}


@Composable
fun IsLoading(isLoading: Boolean, modifier: Modifier) {
    if (isLoading) {
        Box(
            modifier = modifier.size(48.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
            Icon(
                painter = painterResource(id = com.google.android.exoplayer2.ui.R.drawable.exo_icon_pause),
                modifier = modifier.size(35.dp),
                contentDescription = null
            )
        }
    }
}
