package com.example.audius.android.ui.trendinglistscreen

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaMetadataCompat
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.exoplayer.isPlayEnabled
import com.example.audius.android.exoplayer.isPlaying
import com.example.audius.android.exoplayer.isPrepared
import com.example.audius.android.exoplayer.utils.Constants
import com.example.audius.android.ui.bottombars.PlayerBottomBar
import com.example.audius.datalayer.models.SongIconList
import com.example.audius.viewmodel.screens.trending.TrendingListState

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
                                musicServiceConnection.subscribe(Constants.CLICKED_PLAYLIST, object : MediaBrowserCompat.SubscriptionCallback() {})
                              //  play(musicServiceConnection = musicServiceConnection, item.id)
                                onLastItemClick(item.id, item.songIconList)
                            }
                        )
                    })
                }
                if (trendingListState.playMusic) {
                    if (trendingListState.songId != "") {

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
