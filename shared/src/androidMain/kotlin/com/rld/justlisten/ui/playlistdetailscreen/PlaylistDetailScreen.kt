package com.rld.justlisten.ui.playlistdetailscreen

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.asImage
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.loadingscreen.LoadingScreen
import com.rld.justlisten.ui.playlistdetailscreen.components.AnimatedToolBar
import com.rld.justlisten.ui.playlistdetailscreen.components.BottomScrollableContent
import com.rld.justlisten.ui.utils.playMusic
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState

import com.rld.justlisten.ui.actions.PlaylistDetailAction

@Composable
fun PlaylistDetailScreen(
    playlistDetailState: PlaylistDetailState,
    musicPlayer: MusicPlayer,
    onAction: (PlaylistDetailAction) -> Unit
) {
    if (playlistDetailState.isLoading) {
        LoadingScreen()
    } else {
        val context = LocalPlatformContext.current
        val placeholderColor = MaterialTheme.colorScheme.secondaryContainer.toArgb()
        val painter = rememberAsyncImagePainter(
            model = remember(playlistDetailState.playlistIcon, context, placeholderColor) {
                ImageRequest.Builder(context = context)
                    .placeholder(ColorDrawable(placeholderColor).asImage())
                    .data(playlistDetailState.playlistIcon).build()
            }
        )

        val toolbarOffsetHeightPx = remember { mutableStateOf(0f) }

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    // try to consume before LazyColumn to collapse toolbar if needed, hence pre-scroll
                    val delta = available.y
                    val newOffset = toolbarOffsetHeightPx.value + delta
                    if (newOffset < 0 && playlistDetailState.songPlaylist.size >= 5) {
                        toolbarOffsetHeightPx.value = newOffset
                    }
                    // here's the catch: let's pretend we consumed 0 in any case, since we want
                    // LazyColumn to scroll anyway for good UX
                    // We're basically watching scroll without taking it
                    return Offset.Zero
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {

            BottomScrollableContent(
                playlistDetailState = playlistDetailState,
                scrollState = toolbarOffsetHeightPx,
                playlist = playlistDetailState.songPlaylist,
                onShuffleClicked = {
                    playMusic(musicPlayer, playlistDetailState.songPlaylist)
                },
                onSongClicked = { onAction(PlaylistDetailAction.SongPressed(it)) },
                onFavoritePressed = { id, title, user, icon, isFav -> 
                    onAction(PlaylistDetailAction.FavoritePressed(id, title, user, icon, isFav)) 
                },
                painter = painter
            )
            AnimatedToolBar(
                playlistDetailState, 
                toolbarOffsetHeightPx, 
                onBackButtonPressed = { onAction(PlaylistDetailAction.BackPressed(it)) }, 
                onDeletePlaylistClicked = { onAction(PlaylistDetailAction.DeletePlaylistClicked(it)) }
            )
        }
    }
}

