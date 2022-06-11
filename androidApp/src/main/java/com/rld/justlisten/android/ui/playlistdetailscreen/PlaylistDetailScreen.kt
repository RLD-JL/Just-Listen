package com.rld.justlisten.android.ui.playlistdetailscreen

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.rld.justlisten.android.exoplayer.MusicServiceConnection
import com.rld.justlisten.android.ui.loadingscreen.LoadingScreen
import com.rld.justlisten.android.ui.playlistdetailscreen.components.AnimatedToolBar
import com.rld.justlisten.android.ui.playlistdetailscreen.components.BottomScrollableContent
import com.rld.justlisten.android.ui.utils.playMusic
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState

@Composable
fun PlaylistDetailScreen(
    playlistDetailState: PlaylistDetailState, onBackButtonPressed: (Boolean) -> Unit,
    musicServiceConnection: MusicServiceConnection,
    onSongPressed: (String) -> Unit,
    onFavoritePressed: (String, String, UserModel, SongIconList, Boolean) -> Unit,
) {
    if (playlistDetailState.isLoading) {
        LoadingScreen()
    } else {
        val isPlayerReady: MutableState<Boolean> = remember {
            mutableStateOf(false)
        }

        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context = LocalContext.current)
                .placeholder(ColorDrawable(MaterialTheme.colors.secondaryVariant.toArgb()))
                .data(playlistDetailState.playlistIcon).build()
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
                    playMusic(
                        musicServiceConnection,
                        playlistDetailState.songPlaylist,
                        isPlayerReady.value
                    )
                    isPlayerReady.value = true
                },
                onSongClicked = onSongPressed,
                onFavoritePressed = onFavoritePressed,
                painter = painter
            )
            AnimatedToolBar(playlistDetailState, toolbarOffsetHeightPx, onBackButtonPressed)
        }
    }
}

