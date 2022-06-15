package com.rld.justlisten.android.ui.playlistdetailscreen.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import coil.compose.AsyncImagePainter
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState

@Composable
fun BottomScrollableContent(
    playlistDetailState: PlaylistDetailState,
    scrollState: MutableState<Float>,
    playlist: List<PlaylistItem>,
    onSongClicked: (String) -> Unit,
    onShuffleClicked: () -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
    painter: AsyncImagePainter
) {
    SongListScrollingSection(
        painter = painter,
        playlistDetailState = playlistDetailState,
        scrollState = scrollState,
        playlist = playlist,
        onSongClicked = onSongClicked,
        onShuffleClicked = onShuffleClicked,
        onFavoritePressed = onFavoritePressed,
    )
}