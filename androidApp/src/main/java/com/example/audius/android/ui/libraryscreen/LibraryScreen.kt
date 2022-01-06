package com.example.audius.android.ui.libraryscreen

import android.support.v4.media.MediaMetadataCompat
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.exoplayer.isPlayEnabled
import com.example.audius.android.exoplayer.isPlaying
import com.example.audius.android.exoplayer.isPrepared
import com.example.audius.android.ui.playlistscreen.Header
import com.example.audius.android.ui.playlistscreen.components.PlaylistRowItem
import com.example.audius.viewmodel.screens.library.LibraryState

@Composable
fun LibraryScreen(
    musicServiceConnection: MusicServiceConnection,
    libraryState: LibraryState,
    onPlaylistPressed: (String, String, String, String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column() {
            Header(text = "Last Played")
            RowListOfRecentActivity(libraryState, onPlaylistClicked = { _, _, _, _ ->
                {

                }
            })
            FavoritePlaylist(libraryState, onPlaylistPressed)
        }
    }
}

@Composable
fun RowListOfRecentActivity(
    libraryState: LibraryState,
    onPlaylistClicked: (String, String, String, String) -> Unit
) {

    LazyRow {
        itemsIndexed(items = libraryState.recentSongsItems) { _, playlistItem ->
            PlaylistRowItem(
                playlistItem = playlistItem,
                onPlaylistClicked = onPlaylistClicked,
                painterLoaded = { yolo ->

                }
            )
        }
    }
}


@Composable
fun FavoritePlaylist(
    libraryState: LibraryState,
    onPlaylistPressed: (String, String, String, String) -> Unit
) {
    val songIcon =
        if (libraryState.favoritePlaylistItems.isNotEmpty())
            libraryState.favoritePlaylistItems[0].songIconList.songImageURL480px
        else
            ""
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 30.dp)
            .clickable(
                onClick = {
                    onPlaylistPressed(
                        "Favorite",
                        songIcon,
                        "Favorite",
                        "You"
                    )
                }
            )
    ) {
        Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = null)
        Text(text = "Favorite Playlist")
    }
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
