package com.example.audius.android.ui.bottombars.playbar.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.utils.heightSize
import com.example.audius.android.ui.utils.offsetX
import com.example.audius.android.ui.utils.widthSize
import com.example.audius.datalayer.models.SongIconList
import com.example.audius.datalayer.models.UserModel

@Composable
fun PlayBarSwipeActions(
    songIcon: String, currentFraction: Float, constraints: BoxWithConstraintsScope, title: String,
    musicServiceConnection: MusicServiceConnection, onSkipNextPressed: () -> Unit,
    painterLoaded: (Painter) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val painter = rememberImagePainter(
            request = ImageRequest.Builder(context = LocalContext.current)
                .data(songIcon).allowHardware(false).build(),
            onExecute = { previous, current ->
                (widthSize(
                    currentFraction,
                    constraints.maxWidth.value
                ) >= constraints.maxWidth.value * 0.85f) || previous?.request?.data != current.request.data
            }
        )



        (painter.state as? ImagePainter.State.Success)?.let { successState ->
            painterLoaded(successState.painter)
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