package com.rld.justlisten.ui.bottombars.playbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rld.justlisten.ui.bottombars.playbar.components.PlayerBottomBar
import com.rld.justlisten.ui.bottombars.sheets.BottomSheetScreen
import com.rld.justlisten.ui.bottombars.sheets.SheetLayout
import com.rld.justlisten.viewmodel.player.PlayerUiState
import com.rld.justlisten.ui.actions.PlayerAction
import kotlinx.coroutines.launch

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

    // Track which secondary sheet is open (AddPlaylist)
    var currentBottomSheet: BottomSheetScreen? by remember { mutableStateOf(null) }
    val coroutineScope = rememberCoroutineScope()
    val secondarySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val closeSheet: () -> Unit = {
        if (currentBottomSheet != null) {
            coroutineScope.launch {
                secondarySheetState.hide()
                currentBottomSheet = null
            }
        }
    }
    val openSheet: (BottomSheetScreen) -> Unit = {
        currentBottomSheet = it
    }
    val secondarySheetContent: @Composable (BottomSheetScreen, () -> Unit) -> Unit =
        { currentSheet, onClose ->
            Column(modifier = Modifier.fillMaxSize()) {
                TopSection(
                    title = playbackState.currentMedia?.title.orEmpty(),
                    artist = playbackState.currentMedia?.artist,
                    painter = mutablePainter
                )

                Spacer(modifier = Modifier.height(4.dp))
                androidx.compose.material3.HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
                )

                Box(modifier = Modifier.weight(1f)) {
                    SheetLayout(
                        currentScreen = currentSheet,
                        onCloseBottomSheet = onClose,
                        title = playbackState.currentMedia?.title.orEmpty(),
                        mutablePainter = mutablePainter,
                        openSheet = openSheet,
                        addPlaylistList = uiState.addPlaylistList,
                        onAddPlaylistClicked = { name, desc, isRemote, isPrivate ->
                            onAction(PlayerAction.CreatePlaylist(name, desc, isRemote, isPrivate))
                        },
                        getLatestPlaylist = {
                            onAction(PlayerAction.LoadPlaylists)
                        },
                        clickedToAddSongToPlaylist = { playlistTitle, playlistDescription, songList ->
                            onAction(
                                PlayerAction.AddSongToPlaylist(
                                    playlistTitle,
                                    playlistDescription,
                                    songList
                                )
                            )
                        },
                        onUserProfileClick = { userId, userName ->
                            currentBottomSheet = null
                            onUiEvent(PlayerUiEvent.NavigateToArtist(userId, userName))
                        },
                        currentSongId = playbackState.currentMedia?.id
                    )
                }
            }
        }

    // Root box fills whatever space the AnchoredDraggable gives it
    Box(modifier = Modifier.fillMaxSize()) {

        // Main player content
        PlayerBottomBar(
            uiState = uiState,
            layoutInfo = layoutInfo,
            onAction = onAction,
            onUiEvent = { event ->
                when (event) {
                    PlayerUiEvent.CloseSheet -> closeSheet()
                    PlayerUiEvent.OpenAddPlaylist -> openSheet(BottomSheetScreen.AddPlaylist)
                    is PlayerUiEvent.OpenComments -> openSheet(BottomSheetScreen.Comments(event.trackId))
                    is PlayerUiEvent.PainterLoaded -> {
                        mutablePainter.value = event.painter
                    }
                    else -> onUiEvent(event) // Forward visual Collapse, Expand, DominantColor up to Scaffold
                }
            }
        )

        currentBottomSheet?.let { currentSheet ->
            when (currentSheet) {
                is BottomSheetScreen.Comments -> {
                    DraggableCommentsSheet(
                        onDismissRequest = { currentBottomSheet = null },
                    ) { dismiss ->
                        secondarySheetContent(currentSheet, dismiss)
                    }
                }

                else -> ModalBottomSheet(
                    onDismissRequest = { currentBottomSheet = null },
                    sheetState = secondarySheetState,
                    containerColor = MaterialTheme.colorScheme.background,
                    scrimColor = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                    dragHandle = { BottomSheetDefaults.DragHandle() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.75f)
                    ) {
                        secondarySheetContent(currentSheet, closeSheet)
                    }
                }
            }
        }
    }
}

@Composable
fun TopSection(title: String, artist: String?, painter: State<Painter?>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        painter.value?.let {
            Image(
                painter = it,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!artist.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
