package com.example.audius.android.ui.bottombars.playbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.example.audius.android.exoplayer.MusicServiceConnection
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
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

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )

    val coroutines = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                Text(text = "sheetContent")
            }
        },
        sheetPeekHeight = 0.dp
    ) {
        PlayerBottomBar(
            onCollapsedClicked = onCollapsedClicked,
            bottomPadding = bottomPadding,
            currentFraction = currentFraction,
            songIcon = songIcon, title = title,
            musicServiceConnection = musicServiceConnection,
            onSkipNextPressed = onSkipNextPressed,
            onMoreClicked = {coroutines.launch {
                scaffoldState.bottomSheetState.expand()
            }}
        )
    }



}