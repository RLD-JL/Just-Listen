package com.example.audius.android.ui.bottombars

import android.media.session.PlaybackState
import android.support.v4.media.MediaMetadataCompat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import com.example.audius.android.R
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.exoplayer.isPlayEnabled
import com.example.audius.android.exoplayer.isPlaying
import com.example.audius.android.exoplayer.isPrepared

@Composable
fun PlayerBottomBar(
    modifier: Modifier,
    songIcon: String,
    title: String,
    onSkipNextPressed: () -> Unit,
    musicServiceConnection: MusicServiceConnection
) {
    val bottomBarHeight = 57.dp
    Row(
        modifier = modifier
            .padding(bottom = bottomBarHeight)
            .fillMaxWidth()
            .background(color = SnackbarDefaults.backgroundColor),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = rememberImagePainter(songIcon),
            modifier = Modifier.size(65.dp),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        Text(
            text = title,
            style = MaterialTheme.typography.h6.copy(fontSize = 14.sp),
            modifier = Modifier
                .padding(8.dp)
                .weight(1f),
        )
        Icon(
            imageVector = Icons.Default.FavoriteBorder, modifier = Modifier.padding(8.dp),
            contentDescription = null
        )
        if (musicServiceConnection.playbackState.value?.state != PlaybackState.STATE_PLAYING) {
            Icon(
                painter = painterResource(id = R.drawable.exo_icon_play), modifier = Modifier.padding(8.dp).clickable(
                    onClick = {musicServiceConnection.transportControls.play()}
                ),
                contentDescription = null
            )
        } else {
            Icon(
                painter = painterResource(id = R.drawable.exo_icon_pause), modifier = Modifier.padding(8.dp).clickable(
                    onClick = {musicServiceConnection.transportControls.pause()}
                ),
                contentDescription = null
            )
        }

        Icon(
            painter = painterResource(id = R.drawable.exo_ic_skip_next),
            modifier = Modifier
                .padding(8.dp)
                .clickable(onClick = onSkipNextPressed),
            contentDescription = null,
        )
    }
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

