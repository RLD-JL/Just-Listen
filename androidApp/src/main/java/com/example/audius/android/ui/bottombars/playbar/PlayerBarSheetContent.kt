package com.example.audius.android.ui.bottombars.playbar

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.bottombars.playbar.components.addplaylist.AddPlaylistOption
import com.example.audius.android.ui.bottombars.playbar.components.more.PlayBarMoreAction
import com.example.audius.android.ui.bottombars.sheetcontent.BottomSheetScreen
import com.example.audius.datalayer.localdb.addplaylistscreen.AddPlaylist
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
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    addPlaylistList: List<AddPlaylist>
) {
    val songIcon =
        musicServiceConnection.currentPlayingSong.value?.description?.iconUri.toString()
    val title =
        musicServiceConnection.currentPlayingSong.value?.description?.title.toString()

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )

    val mutablePainter = remember { mutableStateOf<Painter?>(null) }

    val coroutines = rememberCoroutineScope()

    var currentBottomSheet: BottomSheetScreen? by remember { mutableStateOf(null) }

    val closeSheet: () -> Unit = {
        coroutines.launch {
            if (scaffoldState.bottomSheetState.isExpanded) {
                scaffoldState.bottomSheetState.collapse()
            }
        }
    }

    val openSheet: (BottomSheetScreen) -> Unit = {
        coroutines.launch {
            currentBottomSheet = it
            scaffoldState.bottomSheetState.expand()
        }
    }

    if (scaffoldState.bottomSheetState.isCollapsed)
        currentBottomSheet = null

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            currentBottomSheet?.let { currentSheet ->
                SheetLayout(currentSheet, closeSheet, title, mutablePainter, openSheet, addPlaylistList)
            }
        },
        sheetPeekHeight = 1.dp
    ) {
        PlayerBottomBar(
            onCollapsedClicked = onCollapsedClicked,
            bottomPadding = bottomPadding,
            currentFraction = currentFraction,
            songIcon = songIcon, title = title,
            musicServiceConnection = musicServiceConnection,
            onSkipNextPressed = onSkipNextPressed,
            onMoreClicked = {
                openSheet(BottomSheetScreen.More)
            },
            onBackgroundClicked = {
                if (scaffoldState.bottomSheetState.isExpanded) {
                    closeSheet()
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

@Composable
fun SheetLayout(
    currentScreen: BottomSheetScreen,
    onCloseBottomSheet: () -> Unit,
    title: String,
    mutablePainter: MutableState<Painter?>,
    openSheet: (BottomSheetScreen) -> Unit,
    addPlaylistList: List<AddPlaylist>
) {
    when (currentScreen) {
        BottomSheetScreen.AddPlaylist -> AddPlaylistOption(title, mutablePainter, addPlaylistList)
        BottomSheetScreen.More -> PlayBarMoreAction(
            title,
            mutablePainter
        ) { onCloseBottomSheet()
            openSheet(BottomSheetScreen.AddPlaylist)
        }
    }
}