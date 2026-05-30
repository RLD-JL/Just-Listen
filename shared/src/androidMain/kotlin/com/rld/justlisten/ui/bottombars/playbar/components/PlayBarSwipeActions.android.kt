package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.foundation.Image
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.components.AnimatedShimmer
import com.rld.justlisten.ui.utils.heightSize
import com.rld.justlisten.ui.utils.lerp
import com.rld.justlisten.ui.utils.offsetX
import com.rld.justlisten.ui.utils.widthSize
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel

@Composable
actual fun PlayBarSwipeActions(
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
            model = ImageRequest.Builder(LocalContext.current)
                .data(highResIcon.ifEmpty { songIcon })
                .placeholderMemoryCacheKey(songIcon)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .crossfade(true)
                .build()
        )

        val state = painter.state
        if (state is coil.compose.AsyncImagePainter.State.Success) {
            painterLoaded(state.painter)
        }

        Box(
            modifier = Modifier
                .size(width = width, height = height)
                .offset(x = offsetX(currentFraction, constraints.maxWidth.value).dp)
                .clip(RoundedCornerShape(lerp(0f, 12f, currentFraction).dp))
        ) {
            Image(
                painter = painter,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (state is coil.compose.AsyncImagePainter.State.Loading) {
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
