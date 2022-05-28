package com.rld.justlisten.android.ui.bottombars.playbar.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import coil.size.Size
import com.rld.justlisten.android.exoplayer.MusicServiceConnection
import com.rld.justlisten.android.ui.components.AnimatedShimmer
import com.rld.justlisten.android.ui.utils.heightSize
import com.rld.justlisten.android.ui.utils.offsetX
import com.rld.justlisten.android.ui.utils.widthSize
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel

@Composable
fun PlayBarSwipeActions(
    songIcon: String, currentFraction: Float, constraints: BoxWithConstraintsScope, title: String,
    musicServiceConnection: MusicServiceConnection, onSkipNextPressed: () -> Unit,
    painterLoaded: (Painter) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    newDominantColor: (Int) -> Unit,
    playBarMinimizedClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(songIcon)
                .allowHardware(false)
                .size(Size.ORIGINAL)
                .build(),
            modifier = Modifier
                .size(
                    width = widthSize(currentFraction, constraints.maxWidth.value).dp,
                    height = heightSize(currentFraction, constraints.maxHeight.value).dp
                )
                .offset(x = offsetX(currentFraction, constraints.maxWidth.value).dp),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            success = { state ->
                painterLoaded(state.painter)
                val drawable = state.result.drawable
                Palette.Builder(drawable.toBitmap()).generate { palette ->
                    palette?.dominantSwatch?.let { swatch ->
                        newDominantColor(swatch.rgb)
                    }
                }
                SubcomposeAsyncImageContent()
            },
            loading = {
                if (currentFraction == 1f) {
                    AnimatedShimmer(
                        width = widthSize(
                            currentFraction,
                            constraints.maxWidth.value
                        ).dp, height = heightSize(currentFraction, constraints.maxHeight.value).dp
                    )
                }
            }
        )
        PlayBarActionsMinimized(
            currentFraction,
            musicServiceConnection,
            title,
            onSkipNextPressed,
            onFavoritePressed,
            songIcon,
            playBarMinimizedClicked = playBarMinimizedClicked
        )
    }
}