package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.rememberSplineBasedDecay
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import com.rld.justlisten.ui.LocalMusicPlayer
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.gestures.animateTo
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MusicNote
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
import coil3.compose.LocalPlatformContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.rld.justlisten.media.MediaMetadata
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.components.MusicLoadingSpinner
import com.rld.justlisten.media.PlaybackStatus
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class TabsSheetState {
    COLLAPSED,
    EXPANDED
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PlayerBottomTabs(
    currentFraction: Float,
    maxHeight: Dp,
    bottomPadding: Dp
) {
    val musicPlayer = LocalMusicPlayer.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("UP NEXT", "LYRICS", "RELATED")
    val playlist by musicPlayer.currentPlaylist.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    if (currentFraction > 0.8f) {
        val density = LocalDensity.current
        val startAnchor = with(density) { 120.dp.toPx() }
        val endAnchor = with(density) { (maxHeight - 56.dp).toPx() }
        val decayAnimationSpec = rememberSplineBasedDecay<Float>()

        val anchoredDraggableState = remember(endAnchor) {
            AnchoredDraggableState(
                initialValue = TabsSheetState.COLLAPSED,
                anchors = DraggableAnchors {
                    TabsSheetState.EXPANDED at startAnchor
                    TabsSheetState.COLLAPSED at endAnchor
                },
                positionalThreshold = { distance: Float -> distance * 0.3f },
                velocityThreshold = { with(density) { 100.dp.toPx() } },
                snapAnimationSpec = spring(stiffness = 300f, dampingRatio = 0.8f),
                decayAnimationSpec = decayAnimationSpec
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(maxHeight - 120.dp)
                .offset {
                    IntOffset(
                        0,
                        anchoredDraggableState.offset.toInt()
                    )
                }
                .anchoredDraggable(
                    state = anchoredDraggableState,
                    orientation = Orientation.Vertical
                )
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(Color.Black.copy(alpha = 0.95f))
                .clickable(enabled = false) {}
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Drag handle header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clickable {
                            coroutineScope.launch {
                                val target =
                                    if (anchoredDraggableState.currentValue == TabsSheetState.COLLAPSED)
                                        TabsSheetState.EXPANDED
                                    else
                                        TabsSheetState.COLLAPSED
                                anchoredDraggableState.animateTo(target)
                            }
                        }
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(40.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.White.copy(alpha = 0.3f))
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            tabs.forEachIndexed { index, tab ->
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedTab = index }
                                ) {
                                    Text(
                                        text = tab,
                                        color = if (selectedTab == index) Color.White else Color.White.copy(
                                            alpha = 0.6f
                                        ),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 1.dp)

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTab) {
                        0 -> { // UP NEXT
                            UpNextQueueView(playlist = playlist)
                        }

                        1 -> { // LYRICS
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No lyrics found.", color = Color.White)
                            }
                        }

                        2 -> { // RELATED
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("No related tracks.", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UpNextQueueView(
    playlist: List<MediaMetadata>,
) {
    val musicPlayer = LocalMusicPlayer.current
    val playbackState by musicPlayer.playbackState.collectAsState()
    val currentMedia = playbackState.currentMedia

    // We maintain a local list to prevent glitchy drag-and-drop due to state flow delays
    var localPlaylist by remember { mutableStateOf(playlist) }

    // Drag state hoisted to the parent so other items can read it without localPlaylist mutations
    var draggingId by remember { mutableStateOf<String?>(null) }
    var draggingStartIndex by remember { mutableStateOf(-1) }
    var currentDragIndex by remember { mutableStateOf(-1) }

    LaunchedEffect(playlist) {
        if (draggingId == null) {
            localPlaylist = playlist
        }
    }

    val currentIndex = localPlaylist.indexOfFirst { it.id == currentMedia?.id }

    val pastSongs = localPlaylist.take(maxOf(0, currentIndex))
    val currentSong = localPlaylist.getOrNull(currentIndex)
    val nextSongs =
        if (currentIndex >= 0 && currentIndex < localPlaylist.size - 1)
            localPlaylist.drop(currentIndex + 1)
        else
            emptyList()

    val lazyListState = rememberLazyListState()
    val isPlaying = playbackState.status == PlaybackStatus.PLAYING

    LazyColumn(
        state = lazyListState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (pastSongs.isNotEmpty()) {
            item {
                Text(
                    text = "PAST SONGS",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            itemsIndexed(pastSongs, key = { _, song -> song.id }) { relativeIndex, song ->
                val actualIndex = relativeIndex
                TrackCard(
                    song = song,
                    actualIndex = actualIndex,
                    currentIndex = currentIndex,
                    playlistSize = localPlaylist.size,
                    canDrag = true,
                    isPlaying = isPlaying,
                    draggingId = draggingId,
                    draggingStartIndex = draggingStartIndex,
                    currentDragIndex = currentDragIndex,
                    onDragStart = { startIdx ->
                        draggingId = song.id
                        draggingStartIndex = startIdx
                        currentDragIndex = startIdx
                    },
                    onCurrentDragIndexChange = { newIdx ->
                        currentDragIndex = newIdx
                    },
                    onDragEnd = { start, end ->
                        if (start != end) {
                            val mutable = localPlaylist.toMutableList()
                            val item = mutable.removeAt(start)
                            mutable.add(end, item)
                            localPlaylist = mutable
                            musicPlayer.moveTrack(start, end)
                        }
                        draggingId = null
                        draggingStartIndex = -1
                        currentDragIndex = -1
                    },
                    onDragCancel = {
                        draggingId = null
                        draggingStartIndex = -1
                        currentDragIndex = -1
                    }
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        if (currentSong != null) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "CURRENTLY PLAYING",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
                TrackCard(
                    song = currentSong,
                    actualIndex = currentIndex,
                    currentIndex = currentIndex,
                    playlistSize = localPlaylist.size,
                    canDrag = false,
                    isPlaying = isPlaying,
                    draggingId = draggingId,
                    draggingStartIndex = draggingStartIndex,
                    currentDragIndex = currentDragIndex,
                    onDragStart = { },
                    onCurrentDragIndexChange = { },
                    onDragEnd = { _, _ -> },
                    onDragCancel = { }
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        if (nextSongs.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "UP NEXT",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            itemsIndexed(nextSongs, key = { _, song -> song.id }) { relativeIndex, song ->
                val actualIndex = (if (currentIndex >= 0) currentIndex + 1 else 0) + relativeIndex
                TrackCard(
                    song = song,
                    actualIndex = actualIndex,
                    currentIndex = currentIndex,
                    playlistSize = localPlaylist.size,
                    canDrag = true,
                    isPlaying = isPlaying,
                    draggingId = draggingId,
                    draggingStartIndex = draggingStartIndex,
                    currentDragIndex = currentDragIndex,
                    onDragStart = { startIdx ->
                        draggingId = song.id
                        draggingStartIndex = startIdx
                        currentDragIndex = startIdx
                    },
                    onCurrentDragIndexChange = { newIdx ->
                        currentDragIndex = newIdx
                    },
                    onDragEnd = { start, end ->
                        if (start != end) {
                            val mutable = localPlaylist.toMutableList()
                            val item = mutable.removeAt(start)
                            mutable.add(end, item)
                            localPlaylist = mutable
                            musicPlayer.moveTrack(start, end)
                        }
                        draggingId = null
                        draggingStartIndex = -1
                        currentDragIndex = -1
                    },
                    onDragCancel = {
                        draggingId = null
                        draggingStartIndex = -1
                        currentDragIndex = -1
                    }
                )
            }
        }
    }
}

@Composable
fun TrackCard(
    song: MediaMetadata,
    actualIndex: Int,
    currentIndex: Int,
    playlistSize: Int,
    canDrag: Boolean,
    isPlaying: Boolean,
    draggingId: String?,
    draggingStartIndex: Int,
    currentDragIndex: Int,
    onDragStart: (Int) -> Unit,
    onCurrentDragIndexChange: (Int) -> Unit,
    onDragEnd: (Int, Int) -> Unit,
    onDragCancel: () -> Unit
) {
    val currentActualIndex by rememberUpdatedState(actualIndex)
    val currentCurrentIndex by rememberUpdatedState(currentIndex)
    val currentPlaylistSize by rememberUpdatedState(playlistSize)

    val musicPlayer = LocalMusicPlayer.current
    var swipeOffset by remember { mutableStateOf(0f) }
    val swipeOffsetAnim by animateFloatAsState(targetValue = swipeOffset)
    val density = LocalDensity.current

    val isBeingDragged = draggingId == song.id
    var draggingOffset by remember { mutableStateOf(0f) }

    // FIX: Use density-safe item height calculation via remember to avoid recomputing each frame
    val itemHeightPx = remember(density) {
        with(density) { (72.dp + 8.dp).toPx() }
    }

    val targetOffset = remember(isBeingDragged, actualIndex, draggingStartIndex, currentDragIndex) {
        if (isBeingDragged) {
            0f
        } else if (draggingId != null) {
            if (draggingStartIndex < currentDragIndex
                && actualIndex > draggingStartIndex
                && actualIndex <= currentDragIndex
            ) {
                -itemHeightPx
            } else if (draggingStartIndex > currentDragIndex
                && actualIndex >= currentDragIndex
                && actualIndex < draggingStartIndex
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
    val finalYOffset = if (isBeingDragged) draggingOffset else animatedOffset

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer {
                translationY = finalYOffset
                shadowElevation = if (isBeingDragged) 12.dp.toPx() else 0f
            }
            .clip(RoundedCornerShape(12.dp))
            .height(72.dp)
    ) {
        // Background: Red Swipe-to-Delete Action Panel
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .width(80.dp)
                .background(Color(0xFFE91E63))
                .clickable {
                    swipeOffset = 0f
                    musicPlayer.removeTrack(actualIndex)
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete track",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        // Foreground: The actual track card content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(swipeOffsetAnim.toInt(), 0) }
                .background(
                    if (actualIndex == currentIndex) Color(0xFF2C2C2C) else Color(0xFF1E1E1E)
                )
                .clickable {
                    if (actualIndex != currentIndex) {
                        musicPlayer.playMedia(song.id)
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            val threshold = with(density) { -40.dp.toPx() }
                            swipeOffset =
                                if (swipeOffset < threshold) with(density) { -80.dp.toPx() } else 0f
                        },
                        onDragCancel = {
                            swipeOffset = 0f
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = swipeOffset + dragAmount
                            val limit = with(density) { -80.dp.toPx() }
                            swipeOffset = newOffset.coerceIn(limit, 0f)
                        }
                    )
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
                            .pointerInput(song.id) {
                                var localStartIdx = -1
                                var localCurrentDragIdx = -1
                                detectDragGestures(
                                    onDragStart = {
                                        swipeOffset = 0f
                                        draggingOffset = 0f
                                        localStartIdx = currentActualIndex
                                        localCurrentDragIdx = localStartIdx
                                        onDragStart(localStartIdx)
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
                                            onCurrentDragIndexChange(newDragIndex)
                                        }
                                    },
                                    onDragEnd = {
                                        draggingOffset = 0f
                                        onDragEnd(localStartIdx, localCurrentDragIdx)
                                        localStartIdx = -1
                                        localCurrentDragIdx = -1
                                    },
                                    onDragCancel = {
                                        draggingOffset = 0f
                                        onDragCancel()
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
    }
}