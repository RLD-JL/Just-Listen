package com.rld.justlisten.ui.bottombars.playbar

import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.Modifier
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.bottombars.playbar.components.PlayerBottomBar
import com.rld.justlisten.ui.bottombars.sheets.BottomSheetScreen
import com.rld.justlisten.ui.bottombars.sheets.SheetLayout
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import kotlinx.coroutines.launch

@ExperimentalMaterialApi
@Composable
fun PlayerBarSheetContent(
    bottomPadding: Dp,
    currentFraction: Float,
    isExtended: Boolean,
    onSkipNextPressed: () -> Unit,
    musicPlayer: MusicPlayer,
    onCollapsedClicked: () -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    addPlaylistList: List<AddPlaylist>,
    onAddPlaylistClicked: (String, String?) -> Unit,
    getLatestPlaylist: () -> Unit,
    clickedToAddSongToPlaylist: (String, String?, List<String>) -> Unit,
    newDominantColor: (Int) -> Unit,
    playBarMinimizedClicked: () -> Unit,
) {
    val playbackState by musicPlayer.playbackState.collectAsState()
    val songIcon by remember { derivedStateOf { playbackState.currentMedia?.lowResArtworkUrl ?: playbackState.currentMedia?.artworkUrl ?: "" } }
    val artworkUrl by remember { derivedStateOf { playbackState.currentMedia?.artworkUrl ?: "" } }
    val title by remember { derivedStateOf { playbackState.currentMedia?.title ?: "" } }

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
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(if (currentBottomSheet != null) 0.7f else 0.01f)
            ) {
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
                    )
                }
            }
        },
        sheetPeekHeight = 0.dp
    ) {
        PlayerBottomBar(
            onCollapsedClicked = onCollapsedClicked,
            bottomPadding = bottomPadding,
            currentFraction = currentFraction,
            isExtended = isExtended,
            songIcon = songIcon,
            artworkUrl = artworkUrl,
            title = title,
            musicPlayer = musicPlayer,
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
            onSaveClicked = {
                openSheet(BottomSheetScreen.AddPlaylist)
            },
            newDominantColor = newDominantColor
        )
    }
}
