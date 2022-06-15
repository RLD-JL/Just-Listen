package com.rld.justlisten.android.ui.playlistdetailscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.rld.justlisten.android.ui.components.AnimatedShimmer
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem

@Composable
fun SongListItem(
    playlistItem: PlaylistItem, onSongClicked: (String) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    playlist: String
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
        SubcomposeAsyncImage(model = ImageRequest.Builder(LocalContext.current)
            .data(playlistItem.songIconList.songImageURL150px)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(55.dp)
                .padding(4.dp),
            loading = {
                AnimatedShimmer(width = 55.dp, height = 55.dp)
            }
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

        if (playlist == "Most Played") {
            Text(text = playlistItem.songCounter)
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

