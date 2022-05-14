package com.rld.justlisten.android.ui.playlistdetailscreen.components

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
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

@OptIn(ExperimentalCoilApi::class)
@Composable
fun SongListItem(
    playlistItem: PlaylistItem, onSongClicked: (String) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(8.dp)
            .clickable(
                onClick = {
                    onSongClicked(playlistItem.id)
                }
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context = LocalContext.current)
                .placeholder(ColorDrawable(MaterialTheme.colors.secondaryVariant.toArgb()))
                .data(playlistItem.songIconList.songImageURL150px).allowHardware(false).build()
        )

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
            )
            Text(
                text = "${playlistItem.title} by ${playlistItem.user}",
                style = typography.subtitle2,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        var isFavorite by rememberSaveable { mutableStateOf(playlistItem.isFavorite) }
            Icon(imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier
                    .padding(4.dp)
                    .size(20.dp)
                    .clickable {
                        onFavoritePressed(
                            playlistItem.id, playlistItem.title,
                            UserModel(playlistItem.user), playlistItem.songIconList,
                            !isFavorite
                        )
                        isFavorite = !isFavorite
                    }
            )
    }
}

