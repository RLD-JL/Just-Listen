package com.rld.justlisten.android.ui.bottombars.playbar

import androidx.activity.compose.BackHandler
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.rld.justlisten.android.exoplayer.MusicServiceConnection
import com.rld.justlisten.android.ui.bottombars.playbar.components.PlayerBottomBar
import com.rld.justlisten.android.ui.bottombars.sheets.BottomSheetScreen
import com.rld.justlisten.android.ui.bottombars.sheets.SheetLayout
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.Events
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@OptIn(InternalCoroutinesApi::class)
@ExperimentalCoilApi
@Composable
fun PlayerBarSheetContent(
    bottomPadding: Dp,
    currentFraction: Float,
    isExtended: Boolean,
    onSkipNextPressed: () -> Unit,
    musicServiceConnection: MusicServiceConnection,
    onCollapsedClicked: () -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    addPlaylistList: List<AddPlaylist>,
    onAddPlaylistClicked: (String, String?) -> Unit,
    getLatestPlaylist: () -> Unit,
    clickedToAddSongToPlaylist: (String, String?, List<String>) -> Unit,
    newDominantColor: (Int) -> Unit,
    playBarMinimizedClicked: () -> Unit,
    events: Events
) {
    val songIcon by remember { derivedStateOf { musicServiceConnection.currentPlayingSong.value?.description?.iconUri.toString() } }
    val title by remember { derivedStateOf {
        musicServiceConnection.currentPlayingSong.value?.description?.title.toString()  } }

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

    if (scaffoldState.bottomSheetState.isExpanded) {
        BackHandler {
            closeSheet()
        }
    }

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetContent = {
            currentBottomSheet?.let { currentSheet ->
                SheetLayout(
                    currentSheet,
                    closeSheet,
                    title,
                    mutablePainter,
                    openSheet,
                    addPlaylistList,
                    onAddPlaylistClicked,
                    getLatestPlaylist,
                    clickedToAddSongToPlaylist = { playlistTitle, playlistDescription, songList ->
                        closeSheet()
                        clickedToAddSongToPlaylist(playlistTitle, playlistDescription, songList)
                    },
                    events
                )
            }
        },
        sheetPeekHeight = 0.dp
    ) {
        PlayerBottomBar(
            onCollapsedClicked = onCollapsedClicked,
            bottomPadding = bottomPadding,
            currentFraction = currentFraction,
            isExtended = isExtended,
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
            playBarMinimizedClicked = playBarMinimizedClicked,
            onFavoritePressed = onFavoritePressed,
            newDominantColor = newDominantColor
        )
    }
}

