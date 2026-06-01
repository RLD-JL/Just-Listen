package com.rld.justlisten.ui.bottombars.playbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import com.rld.justlisten.ui.bottombars.playbar.components.PlayerBottomBar
import com.rld.justlisten.ui.bottombars.sheets.BottomSheetScreen
import com.rld.justlisten.ui.bottombars.sheets.SheetLayout
import com.rld.justlisten.viewmodel.player.PlayerUiState
import com.rld.justlisten.ui.actions.PlayerAction

@ExperimentalMaterial3Api
@Composable
fun PlayerBarSheetContent(
    uiState: PlayerUiState,
    layoutInfo: PlayerLayoutInfo,
    onAction: (PlayerAction) -> Unit,
    onUiEvent: (PlayerUiEvent) -> Unit
) {
    val playbackState = uiState.playbackState ?: com.rld.justlisten.media.PlaybackState(
        status = com.rld.justlisten.media.PlaybackStatus.IDLE,
        currentPosition = 0
    )

    val mutablePainter = remember { mutableStateOf<Painter?>(null) }

    // Track which secondary sheet is open (More / AddPlaylist)
    var currentBottomSheet: BottomSheetScreen? by remember { mutableStateOf(null) }
    val closeSheet: () -> Unit = { currentBottomSheet = null }
    val openSheet: (BottomSheetScreen) -> Unit = { currentBottomSheet = it }

    // Root box fills whatever space the AnchoredDraggable gives it
    Box(modifier = Modifier.fillMaxSize()) {

        // Main player content
        PlayerBottomBar(
            uiState = uiState,
            layoutInfo = layoutInfo,
            onAction = onAction,
            onUiEvent = { event ->
                when (event) {
                    PlayerUiEvent.OpenMore -> openSheet(BottomSheetScreen.More)
                    PlayerUiEvent.CloseSheet -> closeSheet()
                    PlayerUiEvent.OpenAddPlaylist -> openSheet(BottomSheetScreen.AddPlaylist)
                    is PlayerUiEvent.PainterLoaded -> {
                        mutablePainter.value = event.painter
                    }
                    else -> onUiEvent(event) // Forward visual Collapse, Expand, DominantColor up to Scaffold
                }
            }
        )

        // Secondary sheet overlay (More / AddPlaylist)
        AnimatedVisibility(
            visible = currentBottomSheet != null,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically(
                animationSpec = tween(durationMillis = 300),
                initialOffsetY = { it }
            ) + fadeIn(animationSpec = tween(200)),
            exit = slideOutVertically(
                animationSpec = tween(durationMillis = 250),
                targetOffsetY = { it }
            ) + fadeOut(animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f)
            ) {
                currentBottomSheet?.let { currentSheet ->
                    SheetLayout(
                        currentSheet,
                        closeSheet,
                        playbackState.currentMedia?.title.orEmpty(),
                        mutablePainter,
                        openSheet,
                        uiState.addPlaylistList,
                        onAddPlaylistClicked = { name, desc ->
                            onAction(PlayerAction.CreatePlaylist(name, desc))
                        },
                        getLatestPlaylist = {
                            onAction(PlayerAction.LoadPlaylists)
                        },
                        clickedToAddSongToPlaylist = { playlistTitle, playlistDescription, songList ->
                            closeSheet()
                            onAction(PlayerAction.AddSongToPlaylist(
                                playlistTitle,
                                playlistDescription,
                                songList
                            ))
                        },
                    )
                }
            }
        }
    }
}