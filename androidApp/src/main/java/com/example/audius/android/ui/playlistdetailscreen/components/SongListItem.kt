package com.example.audius.android.ui.playlistdetailscreen.components

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.example.audius.datalayer.models.SongIconList
import com.example.audius.datalayer.models.UserModel
import com.example.audius.viewmodel.screens.playlist.PlaylistItem

@OptIn(ExperimentalCoilApi::class)
@Composable
fun SongListItem(
    playlistItem: PlaylistItem, onSongClicked: (String, String, UserModel, SongIconList) -> Unit,
    dominantColor: (Int) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit
) {

    val dominantColorMutable = remember { mutableStateOf(-123123123) }
    Row(
        modifier = Modifier.padding(8.dp)
            .clickable(
                onClick = {
                    dominantColor(dominantColorMutable.value)
                    onSongClicked(
                        playlistItem.id, playlistItem.title,
                        UserModel(playlistItem.user), playlistItem.songIconList
                    )
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val painter = rememberImagePainter(
            request = ImageRequest.Builder(context = LocalContext.current)
                .placeholder(ColorDrawable(MaterialTheme.colors.secondary.toArgb()))
                .data(playlistItem.songIconList.songImageURL150px).allowHardware(false).build()
        )

        (painter.state as? ImagePainter.State.Success)?.let { successState ->
            LaunchedEffect(painter) {
                val drawable = successState.result.drawable
                Palette.Builder(drawable.toBitmap()).generate { palette ->
                    palette?.dominantSwatch?.let {
                        dominantColorMutable.value = it.rgb
                    }
                }
            }

        }

        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(55.dp)
                .padding(4.dp)
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .weight(1f)
        ) {
            Text(
                text = playlistItem.playlistTitle,
                style = typography.h6.copy(fontSize = 16.sp),
                color = MaterialTheme.colors.onSurface
            )
            Text(
                text = "${playlistItem.title} by ${playlistItem.user}",
                style = typography.subtitle2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

            if (playlistItem.isFavorite) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = Color.Red,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp).clickable {
                            onFavoritePressed(
                                playlistItem.id, playlistItem.title,
                                UserModel(playlistItem.user), playlistItem.songIconList,
                                !playlistItem.isFavorite
                            )
                            playlistItem.isFavorite =!playlistItem.isFavorite
                        }
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(4.dp)
                        .size(20.dp).clickable {
                            onFavoritePressed(
                                playlistItem.id, playlistItem.title,
                                UserModel(playlistItem.user), playlistItem.songIconList,
                                !playlistItem.isFavorite
                            )
                            playlistItem.isFavorite =!playlistItem.isFavorite
                        }
                )
            }

        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.padding(4.dp)
        )
    }
}

