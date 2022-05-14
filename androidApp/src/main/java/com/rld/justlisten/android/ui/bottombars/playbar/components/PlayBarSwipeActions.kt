package com.rld.justlisten.android.ui.bottombars.playbar.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.annotation.ExperimentalCoilApi
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.rld.justlisten.android.exoplayer.MusicServiceConnection
import com.rld.justlisten.android.exoplayer.library.extension.id
import com.rld.justlisten.android.ui.utils.heightSize
import com.rld.justlisten.android.ui.utils.offsetX
import com.rld.justlisten.android.ui.utils.widthSize
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel

@OptIn(ExperimentalCoilApi::class)
@Composable
fun PlayBarSwipeActions(
    songIcon: String, currentFraction: Float, constraints: BoxWithConstraintsScope, title: String,
    musicServiceConnection: MusicServiceConnection, onSkipNextPressed: () -> Unit,
    painterLoaded: (Painter) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    newDominantColor: (String, Int) -> Unit,
    ) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context = LocalContext.current)
                .data(songIcon).allowHardware(false).size(Size.ORIGINAL).build(),

        )
        val id = musicServiceConnection.currentPlayingSong.value?.id.toString()

        (painter.state as? AsyncImagePainter.State.Success)?.let { successState ->
            painterLoaded(successState.painter)
            LaunchedEffect(painter) {
                val drawable = successState.result.drawable
                Palette.Builder(drawable.toBitmap()).generate { palette ->
                    palette?.dominantSwatch?.let { swatch ->
                        newDominantColor(id, swatch.rgb)
                    }
                }
            }
        }
        Image(
            painter = painter,
            modifier = Modifier
                .size(
                    width = widthSize(currentFraction, constraints.maxWidth.value).dp,
                    height = heightSize(currentFraction, constraints.maxHeight.value).dp
                )
                .offset(x = offsetX(currentFraction, constraints.maxWidth.value).dp),
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )
        PlayBarActionsMinimized(
            currentFraction,
            musicServiceConnection,
            title,
            onSkipNextPressed,
            onFavoritePressed
        )
    }
}