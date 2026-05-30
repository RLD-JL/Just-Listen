package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel

@Composable
fun PlayBarSwipeActions(
    songIcon: String, highResIcon: String, currentFraction: Float, constraints: BoxWithConstraintsScope, title: String,
    musicPlayer: MusicPlayer, onSkipNextPressed: () -> Unit,
    painterLoaded: (Painter) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    newDominantColor: (Int) -> Unit,
    playBarMinimizedClicked: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.CenterStart
    ) {
        val width = widthSize(currentFraction, constraints.maxWidth.value).dp
        val height = heightSize(currentFraction, constraints.maxHeight.value).dp

        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(highResIcon.ifEmpty { songIcon })
                .placeholderMemoryCacheKey(songIcon)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .crossfade(true)
                .build()
        )

        val state by painter.state.collectAsState()
        if (state is coil3.compose.AsyncImagePainter.State.Success) {
            painterLoaded((state as coil3.compose.AsyncImagePainter.State.Success).painter)
        }

        Box(
            modifier = Modifier
                .size(width = width, height = height)
                .offset(
                    x = offsetX(currentFraction, constraints.maxWidth.value).dp,
                    y = lerp(7.5f, offsetY(currentFraction, constraints.maxHeight.value, 0.12f), currentFraction).dp
                )
                .clip(RoundedCornerShape(lerp(0f, 12f, currentFraction).dp))
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

        PlayBarActionsMinimized(
            currentFraction,
            musicPlayer,
            title,
            onSkipNextPressed,
            onFavoritePressed,
            songIcon,
            playBarMinimizedClicked = playBarMinimizedClicked
        )
    }
}
