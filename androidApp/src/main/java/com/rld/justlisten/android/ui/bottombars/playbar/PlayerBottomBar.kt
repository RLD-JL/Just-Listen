package com.rld.justlisten.android.ui.bottombars.playbar

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rld.justlisten.android.R
import com.rld.justlisten.android.exoplayer.MusicService.Companion.curSongDuration
import com.rld.justlisten.android.exoplayer.MusicServiceConnection
import com.rld.justlisten.android.ui.bottombars.playbar.components.PlayBarActionsMaximized
import com.rld.justlisten.android.ui.bottombars.playbar.components.PlayBarSwipeActions
import com.rld.justlisten.android.ui.bottombars.playbar.components.PlayBarTopSection
import com.rld.justlisten.android.ui.extensions.noRippleClickable
import com.rld.justlisten.android.ui.theme.modifiers.verticalGradientBackground
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import kotlinx.coroutines.InternalCoroutinesApi

@InternalCoroutinesApi
@Composable
fun PlayerBottomBar(
    bottomPadding: Dp,
    currentFraction: Float,
    songIcon: String,
    title: String,
    musicServiceConnection: MusicServiceConnection,
    onSkipNextPressed: () -> Unit,
    onCollapsedClicked: () -> Unit,
    onMoreClicked: () -> Unit,
    onBackgroundClicked: () -> Unit,
    painterLoaded: (Painter) -> Unit,
    dominantColor: Int,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    newDominantColor: (String, Int) -> Unit,
    playBarMinimizedClicked: () -> Unit
) {
    val list = listOf(Color(dominantColor), Color(dominantColor).copy(alpha = 0.6f))
    BoxWithConstraints(
        modifier = if (currentFraction > 0.001) Modifier
            .verticalGradientBackground(list)
            .noRippleClickable { onBackgroundClicked() } else Modifier.noRippleClickable { onBackgroundClicked() }
    ) {
        val constraints = this@BoxWithConstraints
        Column(Modifier.fillMaxSize()) {

            PlayBarTopSection(currentFraction, onCollapsedClicked, onMoreClicked)

            PlayBarSwipeActions(
                songIcon, currentFraction, constraints,
                title, musicServiceConnection, onSkipNextPressed, painterLoaded, onFavoritePressed,
                newDominantColor = newDominantColor,
                playBarMinimizedClicked =  playBarMinimizedClicked
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
                painter = painterResource(id = R.drawable.exo_icon_pause),
                modifier = modifier.size(35.dp),
                contentDescription = null
            )
        }
    }
}
