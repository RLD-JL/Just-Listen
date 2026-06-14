package com.rld.justlisten.ui.bottombars.playbar.components.upnext

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.AsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.rld.justlisten.media.MediaMetadata
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.ui.components.MusicLoadingSpinner
import com.rld.justlisten.ui.components.ConfirmDialog
import com.rld.justlisten.ui.components.AnimatedShimmer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackCard(
    song: MediaMetadata,
    actualIndex: Int,
    currentIndex: Int,
    canDrag: Boolean,
    isPlaying: Boolean,
    dragState: UpNextDragState,
    modifier: Modifier = Modifier,
    onTrackClicked: (() -> Unit)? = null
) {
    val currentActualIndex by rememberUpdatedState(actualIndex)
    val currentCurrentIndex by rememberUpdatedState(currentIndex)
    val currentPlaylistSize by rememberUpdatedState(dragState.playlistSize)

    val musicPlayer = LocalMusicPlayer.current
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()
    val showConfirmDialog = remember { mutableStateOf(false) }

    val isBeingDragged = dragState.draggingId == song.id && dragState.draggingStartIndex == actualIndex
    var draggingOffset by remember { mutableStateOf(0f) }

    val itemHeightPx = remember(density) {
        with(density) { (72.dp + 8.dp).toPx() }
    }

    val targetOffset = remember(isBeingDragged, actualIndex, dragState.draggingStartIndex, dragState.currentDragIndex) {
        if (isBeingDragged) {
            0f
        } else if (dragState.draggingId != null) {
            if (dragState.draggingStartIndex < dragState.currentDragIndex
                && actualIndex > dragState.draggingStartIndex
                && actualIndex <= dragState.currentDragIndex
            ) {
                -itemHeightPx
            } else if (dragState.draggingStartIndex > dragState.currentDragIndex
                && actualIndex >= dragState.currentDragIndex
                && actualIndex < dragState.draggingStartIndex
            ) {
                itemHeightPx
            } else {
                0f
            }
        } else {
            0f
        }
    }

    val animatedOffset by animateFloatAsState(targetValue = targetOffset)
    val finalYOffset = if (dragState.draggingId == null) {
        0f
    } else if (isBeingDragged) {
        draggingOffset
    } else {
        animatedOffset
    }

    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.settledValue) {
        if (dismissState.settledValue == SwipeToDismissBoxValue.EndToStart) {
            showConfirmDialog.value = true
        }
    }

    LaunchedEffect(showConfirmDialog.value) {
        if (!showConfirmDialog.value && dismissState.settledValue == SwipeToDismissBoxValue.EndToStart) {
            dismissState.snapTo(SwipeToDismissBoxValue.Settled)
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = canDrag,
        backgroundContent = {
            val color = when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> Color(0xFFE91E63)
                else -> Color.Transparent
            }
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete track",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (actualIndex == currentIndex) Color(0xFF2C2C2C) else Color(0xFF1E1E1E)
                    )
                    .clickable {
                        if (onTrackClicked != null) {
                            onTrackClicked()
                        } else {
                            if (actualIndex != currentIndex) {
                                musicPlayer.playMedia(song.id)
                            }
                        }
                    }
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Drag Handle on the left
                    if (canDrag) {
                        Icon(
                            imageVector = Icons.Default.DragHandle,
                            contentDescription = "Drag to reorder",
                            tint = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier
                                .size(28.dp)
                                .pointerInput(song.id, actualIndex) {
                                    var localStartIdx = -1
                                    var localCurrentDragIdx = -1
                                    detectDragGestures(
                                        onDragStart = {
                                            coroutineScope.launch {
                                                dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                                            }
                                            draggingOffset = 0f
                                            localStartIdx = currentActualIndex
                                            localCurrentDragIdx = localStartIdx
                                            dragState.startDrag(song.id, localStartIdx)
                                        },
                                        onDrag = { change, dragAmount ->
                                            change.consume()
                                            if (localStartIdx == -1) return@detectDragGestures

                                            draggingOffset += dragAmount.y
                                            
                                            val itemHeightPx = 72.dp.toPx() + 8.dp.toPx()
                                            val slots = if (draggingOffset > 0) {
                                                ((draggingOffset + itemHeightPx / 2) / itemHeightPx).toInt()
                                            } else {
                                                ((draggingOffset - itemHeightPx / 2) / itemHeightPx).toInt()
                                            }
                                            
                                            val cIndex = currentCurrentIndex
                                            val pSize = currentPlaylistSize

                                            var newDragIndex = localStartIdx + slots
                                            if (localStartIdx > cIndex) {
                                                val minIdx = if (cIndex >= 0) cIndex + 1 else 0
                                                val maxIdx = maxOf(minIdx, pSize - 1)
                                                newDragIndex = newDragIndex.coerceIn(minIdx, maxIdx)
                                            } else if (localStartIdx < cIndex) {
                                                val maxIdx = maxOf(0, cIndex - 1)
                                                newDragIndex = newDragIndex.coerceIn(0, maxIdx)
                                            } else {
                                                newDragIndex = localStartIdx
                                            }
                                            
                                            if (newDragIndex != localCurrentDragIdx) {
                                                localCurrentDragIdx = newDragIndex
                                                dragState.updateDragIndex(newDragIndex)
                                            }
                                        },
                                        onDragEnd = {
                                            draggingOffset = 0f
                                            dragState.endDrag(localStartIdx, localCurrentDragIdx)
                                            localStartIdx = -1
                                            localCurrentDragIdx = -1
                                        },
                                        onDragCancel = {
                                            draggingOffset = 0f
                                            dragState.cancelDrag()
                                            localStartIdx = -1
                                            localCurrentDragIdx = -1
                                        }
                                    )
                                }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = "Current song",
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Song Cover Art Image using Coil
                    val context = LocalPlatformContext.current
                    val painter = rememberAsyncImagePainter(
                        model = remember(song.artworkUrl, song.lowResArtworkUrl, context) {
                            ImageRequest.Builder(context)
                                .data(song.artworkUrl ?: song.lowResArtworkUrl ?: "")
                                .memoryCachePolicy(CachePolicy.ENABLED)
                                .diskCachePolicy(CachePolicy.ENABLED)
                                .build()
                        }
                    )
                    val state by painter.state.collectAsState()

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!song.artworkUrl.isNullOrEmpty() || !song.lowResArtworkUrl.isNullOrEmpty()) {
                            Image(
                                painter = painter,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (state is AsyncImagePainter.State.Loading) {
                                AnimatedShimmer(48.dp, 48.dp)
                            }
                        } else {
                            // Premium default placeholder
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF9C27B0), Color(0xFF00BCD4))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MusicNote,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Loading overlay for current track
                        if (actualIndex == currentIndex && isPlaying) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.6f)),
                                contentAlignment = Alignment.Center
                            ) {
                                MusicLoadingSpinner(color = Color(0xFFE91E63), size = 20.dp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Artist Column
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = song.title,
                            color = if (actualIndex == currentIndex) Color(0xFFE91E63) else Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = song.artist,
                            color = Color.White.copy(alpha = 0.6f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer {
                translationY = finalYOffset
                shadowElevation = if (isBeingDragged) 12.dp.toPx() else 0f
            }
            .clip(RoundedCornerShape(12.dp))
            .height(72.dp)
    )

    ConfirmDialog(
        title = "Remove Song",
        description = "Are you sure you want to remove \"${song.title}\" from Up Next?",
        confirmText = "Yes, Remove",
        cancelText = "Cancel",
        openDialog = showConfirmDialog,
        onConfirm = {
            musicPlayer.removeTrack(actualIndex)
        }
    )
}
