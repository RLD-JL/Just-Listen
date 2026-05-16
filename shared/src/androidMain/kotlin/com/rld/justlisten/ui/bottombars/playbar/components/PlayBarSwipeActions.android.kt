package com.rld.justlisten.ui.bottombars.playbar.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.components.AnimatedShimmer
import com.rld.justlisten.ui.utils.heightSize
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val width = widthSize(currentFraction, constraints.maxWidth.value).dp
        val height = heightSize(currentFraction, constraints.maxHeight.value).dp
        
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(if (currentFraction > 0.6f) highResIcon else songIcon)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(width = width, height = height)
                .offset(x = offsetX(currentFraction, constraints.maxWidth.value).dp),
            loading = {
                AnimatedShimmer(width = width, height = height)
            },
            onSuccess = { success ->
                painterLoaded(success.painter)
            }
        )
        
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
