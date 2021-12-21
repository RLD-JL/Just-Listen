package com.example.audius.android.ui.bottombars.playbar

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.audius.android.R
import com.example.audius.android.exoplayer.MusicService.Companion.curSongDuration
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.bottombars.playbar.components.PlayBarActionsMaximized
import com.example.audius.android.ui.bottombars.playbar.components.PlayBarSwipeActions
import com.example.audius.android.ui.bottombars.playbar.components.PlayBarTopSection
import com.example.audius.android.ui.theme.modifiers.verticalGradientBackground
import com.example.audius.android.ui.utils.*
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
) {
    val dominantListOfColor = remember { mutableMapOf<String, List<Color>>() }
    val list = dominantListOfColor[title]
    BoxWithConstraints(
        modifier = if (list?.isNotEmpty() == true) Modifier.verticalGradientBackground(list)
        else Modifier
    ) {
        val constraints = this@BoxWithConstraints
        Column(Modifier.fillMaxSize()) {

            PlayBarTopSection(currentFraction, onCollapsedClicked, onMoreClicked)

            PlayBarSwipeActions(
                songIcon, currentFraction, constraints, dominantListOfColor,
                title, musicServiceConnection, onSkipNextPressed
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
                constraints.maxWidth.value,
            )
        }
    }
}



@Composable
fun IsLoading(isLoading: Boolean, modifier: Modifier) {
    if (isLoading) {
        Box(
            modifier = modifier.size(46.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
            Icon(
                painter = painterResource(id = R.drawable.exo_icon_pause),
                modifier = modifier.size(30.dp),
                contentDescription = null
            )
        }
    }
}
