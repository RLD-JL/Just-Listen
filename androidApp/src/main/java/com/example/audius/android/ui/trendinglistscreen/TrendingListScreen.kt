package com.example.audius.android.ui.trendinglistscreen

import android.support.v4.media.MediaMetadataCompat
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.exoplayer.isPlayEnabled
import com.example.audius.android.exoplayer.isPlaying
import com.example.audius.android.exoplayer.isPrepared
import com.example.audius.android.ui.bottombars.PlayerBottomBar
import com.example.audius.datalayer.models.SongIconList
import com.example.audius.viewmodel.screens.trending.TrendingListState
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

@Composable
fun TrendingListScreen(
    musicServiceConnection: MusicServiceConnection,
    trendingListState: TrendingListState,
    onLastItemClick: (String, SongIconList) -> Unit,
    onSkipNextPressed: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (trendingListState.isLoading) {
            Toast.makeText(LocalContext.current, "loading", Toast.LENGTH_SHORT).show()
        } else {
            if (trendingListState.trendingListItems.isEmpty()) {
                EmptyList()
            } else {
                LazyColumn {
                    items(items = trendingListState.trendingListItems, itemContent = { item ->
                        TrendingListRow(
                            data = item,
                            onLastItemClick = {
                                play(musicServiceConnection = musicServiceConnection, item.id)
                                onLastItemClick(item.id, item.songIconList)
                            }
                        )
                    })
                }
                if (trendingListState.playMusic) {
                    if (trendingListState.songId != "") {
                        PlayerBottomBar(
                            Modifier.align(Alignment.BottomCenter),
                            songIcon = musicServiceConnection.currentPlayingSong.value?.description?.iconUri.toString(),
                            title = musicServiceConnection.currentPlayingSong.value?.description?.mediaId.toString(),
                            onSkipNextPressed = { skipToNext(musicServiceConnection) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyList() {
    Text(
        text = "empty list",
        style = MaterialTheme.typography.body1,
        modifier = Modifier
            .padding(top = 30.dp)
            .fillMaxWidth(),
        textAlign = TextAlign.Center,
        fontSize = 18.sp
    )
}

fun skipToNext(musicServiceConnection: MusicServiceConnection) {
    musicServiceConnection.transportControls.skipToNext()
}


fun play(musicServiceConnection: MusicServiceConnection, mediaId: String) {
    val isPrepared = musicServiceConnection.playbackState.value?.isPrepared ?: false
    if (isPrepared && mediaId ==
        musicServiceConnection.currentPlayingSong.value?.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID)
    ) {
        musicServiceConnection.playbackState.value?.let { playbackState ->
            when {
                playbackState.isPlaying -> musicServiceConnection.transportControls.pause()
                playbackState.isPlayEnabled -> musicServiceConnection.transportControls.play()
                else -> Unit
            }
        }
    } else {
        musicServiceConnection.transportControls.playFromMediaId(mediaId, null)
    }
}
