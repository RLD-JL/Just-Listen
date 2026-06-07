package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.rld.justlisten.ui.components.AnimatedShimmer
import com.rld.justlisten.ui.utils.heightSize
import com.rld.justlisten.ui.utils.lerp
import com.rld.justlisten.ui.utils.offsetX
import com.rld.justlisten.ui.utils.offsetY
import com.rld.justlisten.ui.utils.widthSize
import com.rld.justlisten.ui.utils.image.getImageDominantColor
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.ui.LocalMusicPlayer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.rld.justlisten.ui.components.SmartMarqueeText

@Composable
fun PlayBarSwipeActions(
    songIcon: String,
    highResIcon: String,
    currentFraction: Float,
    constraints: BoxWithConstraintsScope,
    title: String,
    onSkipNextPressed: () -> Unit,
    onSkipPreviousPressed: () -> Unit,
    painterLoaded: (Painter) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    newDominantColor: (Int) -> Unit,
    playBarMinimizedClicked: () -> Unit
) {
    // Ease the fraction — image decelerates as it reaches its destination
    val eased = FastOutSlowInEasing.transform(currentFraction)

    val swipeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Reset swipeOffset if user starts expanding the player
    LaunchedEffect(currentFraction) {
        if (currentFraction > 0.15f && swipeOffset.value != 0f) {
            swipeOffset.snapTo(0f)
        }
    }

    val screenWidth = constraints.maxWidth.value
    val screenHeight = constraints.maxHeight.value
    val swipeableWidth = screenWidth - 144f // 144dp reserved for buttons on the right

    // Fetch next and previous tracks from playlist queue
    val musicPlayer = LocalMusicPlayer.current
    val playlist by musicPlayer.currentPlaylist.collectAsState()
    val playbackState by musicPlayer.playbackState.collectAsState()
    val currentMedia = playbackState.currentMedia

    val currentIndex = remember(playlist, currentMedia) {
        playlist.indexOfFirst { it.id == currentMedia?.id }
    }
    val nextSong = remember(playlist, currentIndex) {
        if (currentIndex != -1 && currentIndex < playlist.size - 1) {
            playlist.getOrNull(currentIndex + 1)
        } else if (currentIndex == playlist.size - 1 && playlist.isNotEmpty()) {
            playlist.firstOrNull()
        } else {
            null
        }
    }
    val prevSong = remember(playlist, currentIndex) {
        if (currentIndex > 0) {
            playlist.getOrNull(currentIndex - 1)
        } else if (currentIndex == 0 && playlist.isNotEmpty()) {
            playlist.lastOrNull()
        } else {
            null
        }
    }

    val swipeOffsetDp = with(density) { swipeOffset.value.toDp() }

    // Horizontal drag modifier to support swiping left/right to skip next/back
    val dragModifier = if (currentFraction < 0.15f) {
        Modifier.pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    val swipeableWidthPx = with(density) { swipeableWidth.dp.toPx() }
                    val thresholdPx = swipeableWidthPx * 0.30f
                    val currentOffset = swipeOffset.value
                    coroutineScope.launch {
                        if (currentOffset > thresholdPx) {
                            // Swipe right -> Previous song
                            // Animate remaining distance to fully center the previous track
                            swipeOffset.animateTo(swipeableWidthPx, spring())
                            // Perform track transition and reset offset instantly
                            onSkipPreviousPressed()
                            swipeOffset.snapTo(0f)
                        } else if (currentOffset < -thresholdPx) {
                            // Swipe left -> Next song
                            // Animate remaining distance to fully center the next track
                            swipeOffset.animateTo(-swipeableWidthPx, spring())
                            // Perform track transition and reset offset instantly
                            onSkipNextPressed()
                            swipeOffset.snapTo(0f)
                        } else {
                            swipeOffset.animateTo(0f, spring())
                        }
                    }
                },
                onDragCancel = {
                    coroutineScope.launch {
                        swipeOffset.animateTo(0f, spring())
                    }
                },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume()
                    coroutineScope.launch {
                        swipeOffset.snapTo(swipeOffset.value + dragAmount)
                    }
                }
            )
        }
    } else {
        Modifier
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart   // anchor to top-left so offset math is predictable
    ) {
        val width  = widthSize(eased, screenWidth, screenHeight).dp
        val height = heightSize(eased, screenWidth, screenHeight).dp

        val context = LocalPlatformContext.current
        val painter = rememberAsyncImagePainter(
            model = remember(highResIcon, songIcon, context) {
                ImageRequest.Builder(context)
                    .data(highResIcon.ifEmpty { songIcon })
                    .placeholderMemoryCacheKey(songIcon)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(false)
                    .build()
            }
        )

        val state by painter.state.collectAsState()
        if (state is coil3.compose.AsyncImagePainter.State.Success) {
            painterLoaded((state as coil3.compose.AsyncImagePainter.State.Success).painter)
        }
        
        LaunchedEffect(state) {
            if (state is coil3.compose.AsyncImagePainter.State.Success) {
                val image = (state as coil3.compose.AsyncImagePainter.State.Success).result.image
                getImageDominantColor(image)?.let { colorInt ->
                    newDominantColor(colorInt)
                }
            }
        }

        // Interpolate container bounds to clip artwork and text when minimized
        val containerWidth = lerp(swipeableWidth, screenWidth, eased).dp
        val containerHeight = lerp(65f, screenHeight, eased).dp

        Box(
            modifier = Modifier
                .width(containerWidth)
                .height(containerHeight)
                .clickable(
                    enabled = currentFraction < 0.1f,
                    onClick = playBarMinimizedClicked
                )
                .then(dragModifier)
                .clipToBounds()
        ) {
            // Next and Previous tracks are only visible and layout-computed when minimized
            if (currentFraction < 0.15f) {
                if (prevSong != null) {
                    MinibarTrackRow(
                        title = prevSong.title,
                        artist = prevSong.artist,
                        artworkUrl = prevSong.artworkUrl,
                        lowResArtworkUrl = prevSong.lowResArtworkUrl,
                        modifier = Modifier
                            .width(swipeableWidth.dp)
                            .fillMaxHeight()
                            .offset(x = -swipeableWidth.dp + swipeOffsetDp)
                    )
                }

                if (nextSong != null) {
                    MinibarTrackRow(
                        title = nextSong.title,
                        artist = nextSong.artist,
                        artworkUrl = nextSong.artworkUrl,
                        lowResArtworkUrl = nextSong.lowResArtworkUrl,
                        modifier = Modifier
                            .width(swipeableWidth.dp)
                            .fillMaxHeight()
                            .offset(x = swipeableWidth.dp + swipeOffsetDp)
                    )
                }
            }

            // Current Track artwork image
            Box(
                modifier = Modifier
                    .size(width = width, height = height)
                    .offset(
                        x = offsetX(eased, screenWidth, screenHeight).dp,
                        y = offsetY(eased, screenWidth, screenHeight).dp
                    )
                    .graphicsLayer {
                        translationX = swipeOffset.value * (1f - currentFraction).coerceIn(0f, 1f)
                    }
                    // Slight rounding even when collapsed so it looks polished
                    .clip(RoundedCornerShape(lerp(6f, 16f, eased).dp))
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                if (state is coil3.compose.AsyncImagePainter.State.Loading) {
                    AnimatedShimmer(width = width, height = height)
                }
            }

            // Current Track text metadata (only shown when minimized, slides with swipeOffset)
            if (currentFraction < 0.15f) {
                Column(
                    modifier = Modifier
                        .width((swipeableWidth - 65f).dp)
                        .fillMaxHeight()
                        .offset(x = 65.dp + swipeOffsetDp)
                        .graphicsLayer {
                            // Fade out text as the player expands
                            alpha = (1f - currentFraction * 3f).coerceIn(0f, 1f)
                        },
                    verticalArrangement = Arrangement.Center
                ) {
                    val currentTitle = currentMedia?.title ?: title
                    val currentArtist = currentMedia?.artist ?: ""
                    SmartMarqueeText(
                        text = currentTitle,
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    if (currentArtist.isNotEmpty()) {
                        SmartMarqueeText(
                            text = currentArtist,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }

        // Fixed Minimized controls (Favorite, Play/Pause, SkipNext)
        PlayBarActionsMinimized(
            currentFraction = currentFraction,
            onSkipNextPressed = onSkipNextPressed,
            onFavoritePressed = onFavoritePressed
        )
    }
}

@Composable
fun MinibarTrackRow(
    title: String,
    artist: String,
    artworkUrl: String?,
    lowResArtworkUrl: String?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.width(8.dp))

        val context = LocalPlatformContext.current
        val painter = rememberAsyncImagePainter(
            model = remember(artworkUrl, lowResArtworkUrl, context) {
                ImageRequest.Builder(context)
                    .data(artworkUrl ?: lowResArtworkUrl ?: "")
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .crossfade(true)
                    .build()
            }
        )

        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(6.dp))
        )

        Spacer(Modifier.width(10.dp))

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            SmartMarqueeText(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                color = Color.White
            )
            if (artist.isNotEmpty()) {
                SmartMarqueeText(
                    text = artist,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}