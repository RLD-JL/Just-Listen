package com.rld.justlisten.ui.bottombars.playbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.rld.justlisten.ui.bottombars.playbar.components.PlayerBottomBar
import com.rld.justlisten.ui.bottombars.sheets.BottomSheetScreen
import com.rld.justlisten.ui.bottombars.sheets.SheetLayout
import com.rld.justlisten.viewmodel.player.PlayerUiState
import com.rld.justlisten.ui.actions.PlayerAction
import kotlinx.coroutines.launch
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.graphics.Color
import com.rld.justlisten.ui.extensions.noRippleClickable

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
    val closeSheet: () -> Unit = { currentBottomSheet = null }
    val openSheet: (BottomSheetScreen) -> Unit = { currentBottomSheet = it }

    val dragOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    val draggableState = rememberDraggableState { delta ->
        coroutineScope.launch {
            dragOffset.snapTo((dragOffset.value + delta).coerceAtLeast(0f))
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < 0f && dragOffset.value > 0f) {
                    val newOffset = (dragOffset.value + delta).coerceAtLeast(0f)
                    val consumed = newOffset - dragOffset.value
                    coroutineScope.launch {
                        dragOffset.snapTo(newOffset)
                    }
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y
                if (delta > 0f) {
                    val newOffset = dragOffset.value + delta
                    coroutineScope.launch {
                        dragOffset.snapTo(newOffset)
                    }
                    return Offset(0f, delta)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                return Velocity.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (dragOffset.value > 150f || available.y > 400f) {
                    closeSheet()
                } else {
                    dragOffset.animateTo(0f, spring())
                }
                return available
            }
        }
    }

    val isEntireSheetDraggable = uiState.addPlaylistList.isEmpty()

    // Reset drag offset when sheet opens
    LaunchedEffect(currentBottomSheet) {
        if (currentBottomSheet != null) {
            dragOffset.snapTo(0f)
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

        // Dimmed Scrim/Backdrop for secondary sheet
        if (currentBottomSheet != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .noRippleClickable { closeSheet() }
            )
        }

        // Secondary sheet overlay
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
                    .fillMaxHeight(if (currentBottomSheet is BottomSheetScreen.Comments) 0.95f else 0.75f)
                    .offset { IntOffset(0, dragOffset.value.toInt()) }
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(MaterialTheme.colorScheme.background)
                    .nestedScroll(nestedScrollConnection)
                    .then(
                        if (isEntireSheetDraggable) {
                            Modifier.draggable(
                                state = draggableState,
                                orientation = Orientation.Vertical,
                                onDragStopped = { velocity ->
                                    if (dragOffset.value > 150f || velocity > 400f) {
                                        closeSheet()
                                    } else {
                                        coroutineScope.launch {
                                            dragOffset.animateTo(0f, spring())
                                        }
                                    }
                                }
                            )
                        } else {
                            Modifier
                        }
                    )
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Draggable Drag Handle & Header Area (only active if not entire sheet is draggable)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (!isEntireSheetDraggable) {
                                    Modifier.draggable(
                                        state = draggableState,
                                        orientation = Orientation.Vertical,
                                        onDragStopped = { velocity ->
                                            if (dragOffset.value > 150f || velocity > 400f) {
                                                closeSheet()
                                            } else {
                                                coroutineScope.launch {
                                                    dragOffset.animateTo(0f, spring())
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    Modifier
                                }
                            )
                    ) {
                        // Premium pill shape handle indicator
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f))
                            )
                        }

                        // Song Details sticky preview
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
                    }

                    // Content Area (SheetLayout)
                    Box(modifier = Modifier.weight(1f)) {
                        currentBottomSheet?.let { currentSheet ->
                            SheetLayout(
                                currentScreen = currentSheet,
                                onCloseBottomSheet = closeSheet,
                                title = playbackState.currentMedia?.title.orEmpty(),
                                mutablePainter = mutablePainter,
                                openSheet = openSheet,
                                addPlaylistList = uiState.addPlaylistList,
                                onAddPlaylistClicked = { name, desc ->
                                    onAction(PlayerAction.CreatePlaylist(name, desc))
                                },
                                getLatestPlaylist = {
                                    onAction(PlayerAction.LoadPlaylists)
                                },
                                clickedToAddSongToPlaylist = { playlistTitle, playlistDescription, songList ->
                                    onAction(PlayerAction.AddSongToPlaylist(
                                        playlistTitle,
                                        playlistDescription,
                                        songList
                                    ))
                                },
                                currentSongId = playbackState.currentMedia?.id
                            )
                        }
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