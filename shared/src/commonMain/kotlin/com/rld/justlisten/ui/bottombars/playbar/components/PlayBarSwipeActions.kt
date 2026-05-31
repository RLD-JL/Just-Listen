package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.components.AnimatedShimmer
import com.rld.justlisten.ui.utils.heightSize
import com.rld.justlisten.ui.utils.lerp
import com.rld.justlisten.ui.utils.offsetX
import com.rld.justlisten.ui.utils.offsetY
import com.rld.justlisten.ui.utils.widthSize
import com.rld.justlisten.ui.utils.image.getImageDominantColor
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel

@Composable
fun PlayBarSwipeActions(
    songIcon: String,
    highResIcon: String,
    currentFraction: Float,
    constraints: BoxWithConstraintsScope,
    title: String,
    onSkipNextPressed: () -> Unit,
    painterLoaded: (Painter) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    newDominantColor: (Int) -> Unit,
    playBarMinimizedClicked: () -> Unit
) {
    // Ease the fraction — image decelerates as it reaches its destination
    val eased = FastOutSlowInEasing.transform(currentFraction)

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopStart   // anchor to top-left so offset math is predictable
    ) {
        val width  = widthSize(eased, constraints.maxWidth.value).dp
        val height = heightSize(eased, constraints.maxHeight.value).dp

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

        Box(
            modifier = Modifier
                .size(width = width, height = height)
                .offset(
                    x = offsetX(eased, constraints.maxWidth.value).dp,
                    y = offsetY(eased, constraints.maxHeight.value).dp
                )
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

        // Minimized controls sit in the same Box, anchored TopStart,
        // they fade out as the player expands
        PlayBarActionsMinimized(
            currentFraction = currentFraction,
            title = title,
            onSkipNextPressed = onSkipNextPressed,
            onFavoritePressed = onFavoritePressed,
            songIcon = songIcon,
            playBarMinimizedClicked = playBarMinimizedClicked
        )
    }
}