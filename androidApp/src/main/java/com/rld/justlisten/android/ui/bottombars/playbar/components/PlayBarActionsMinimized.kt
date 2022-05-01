package com.rld.justlisten.android.ui.bottombars.playbar.components

import android.media.session.PlaybackState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.android.R
import com.rld.justlisten.android.exoplayer.MusicServiceConnection
import com.rld.justlisten.android.ui.bottombars.playbar.IsLoading
import com.rld.justlisten.android.ui.theme.typography
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel

@Composable
fun PlayBarActionsMinimized(
    currentFraction: Float, musicServiceConnection: MusicServiceConnection,
    title: String, onSkipNextPressed: () -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    ) {
    Row(
        Modifier
            .graphicsLayer(alpha = 1f - currentFraction * 2)
            .height(IntrinsicSize.Max)
    ) {
        if (currentFraction != 1f) {
            Text(
                text = title,
                style = typography.h6.copy(fontSize = 10.sp),
                modifier = Modifier
                    .padding(8.dp)
                    .weight(0.7f),
                maxLines = 3
            )
            val songId = musicServiceConnection.currentPlayingSong.value?.description?.mediaId ?: ""
            if (musicServiceConnection.isFavorite[songId] == true)
            {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    tint = Color.Red,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(30.dp).clickable {
                            onFavoritePressed(
                                songId, title,
                                UserModel(), SongIconList(),
                                !musicServiceConnection.isFavorite[songId]!!
                            )
                        },
                    contentDescription = null
                )
            } else {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(30.dp).clickable {
                            onFavoritePressed(
                                songId, title,
                                UserModel(), SongIconList(),
                                !musicServiceConnection.isFavorite[songId]!!
                            )
                        },
                    contentDescription = null
                )
            }
            if (musicServiceConnection.playbackState.value?.state != PlaybackState.STATE_PLAYING &&
                musicServiceConnection.playbackState.value?.state != PlaybackState.STATE_BUFFERING
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.exo_icon_play),
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable(
                            onClick = { musicServiceConnection.transportControls.play() }
                        )
                        .size(35.dp),
                    contentDescription = null
                )
            }
            IsLoading(
                musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_BUFFERING,
                Modifier
            )
            if (musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PLAYING) {
                Icon(
                    painter = painterResource(id = R.drawable.exo_icon_pause),
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable(
                            onClick = { musicServiceConnection.transportControls.pause() }
                        )
                        .size(35.dp),
                    contentDescription = null
                )
            }
            Icon(
                painter = painterResource(id = R.drawable.exo_ic_skip_next),
                modifier = Modifier
                    .padding(8.dp)
                    .clickable(onClick = onSkipNextPressed)
                    .size(35.dp),
                contentDescription = null,
            )
        }
    }
}