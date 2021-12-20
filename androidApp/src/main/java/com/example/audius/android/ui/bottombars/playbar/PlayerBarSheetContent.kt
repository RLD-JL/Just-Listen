package com.example.audius.android.ui.bottombars.playbar

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import coil.annotation.ExperimentalCoilApi
import com.example.audius.android.exoplayer.MusicServiceConnection
import kotlinx.coroutines.InternalCoroutinesApi

@OptIn(InternalCoroutinesApi::class)
@ExperimentalCoilApi
@Composable
fun PlayerBarSheetContent(
    bottomPadding: Dp,
    currentFraction: Float,
    onSkipNextPressed: () -> Unit,
    musicServiceConnection: MusicServiceConnection,
    onCollapsedClicked: () -> Unit,
) {
    val songIcon =
        musicServiceConnection.currentPlayingSong.value?.description?.iconUri.toString()
    val title =
        musicServiceConnection.currentPlayingSong.value?.description?.title.toString()

    PlayerBottomBar(
        onCollapsedClicked = onCollapsedClicked,
        bottomPadding = bottomPadding,
        currentFraction = currentFraction,
        songIcon = songIcon, title = title,
        musicServiceConnection = musicServiceConnection, onSkipNextPressed = onSkipNextPressed
    )

}