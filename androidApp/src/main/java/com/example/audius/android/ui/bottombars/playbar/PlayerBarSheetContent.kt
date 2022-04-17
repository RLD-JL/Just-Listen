package com.example.audius.android.ui.bottombars.playbar

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.bottombars.playbar.components.more.PlayBarMoreAction
import com.example.audius.datalayer.models.SongIconList
import com.example.audius.datalayer.models.UserModel
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
    dominantColor: Int,
    onCollapsedClicked: () -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit
) {
    val songIcon =
        musicServiceConnection.currentPlayingSong.value?.description?.iconUri.toString()
    val title =
        musicServiceConnection.currentPlayingSong.value?.description?.title.toString()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )

    val mutablePainter = remember { mutableStateOf<Painter?>(null) }

    val onMoreClicked = remember { mutableStateOf(false) }

    val coroutines = rememberCoroutineScope()

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            if (onMoreClicked.value)
                PlayBarMoreAction(title, mutablePainter)
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
            onMoreClicked = {
                onMoreClicked.value = true
                coroutines.launch {
                    scaffoldState.bottomSheetState.expand()
                }
            },
            onBackgroundClicked = {
                if (onMoreClicked.value)
                {
                    coroutines.launch {
                        scaffoldState.bottomSheetState.collapse()
                    }
                    onMoreClicked.value = false
                }
            },
            painterLoaded = { painter ->
                mutablePainter.value = painter
            },
            dominantColor = dominantColor,
            onFavoritePressed = onFavoritePressed
        )
    }


}