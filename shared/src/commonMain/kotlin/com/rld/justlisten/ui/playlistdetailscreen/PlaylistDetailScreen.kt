package com.rld.justlisten.ui.playlistdetailscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.rld.justlisten.ui.addplaylistscreen.components.ConfirmDeletePlaylistDialog
import com.rld.justlisten.ui.addplaylistscreen.components.EditPlaylistDialog

@Composable
fun PlaylistDetailScreen(
    playlistDetailState: PlaylistDetailState,
    musicPlayer: MusicPlayer,
    onAction: (PlaylistDetailAction) -> Unit
) {
    if (playlistDetailState.showConnectPrompt) {
        com.rld.justlisten.ui.artistprofile.components.ConnectPromptDialog(
            onDismissRequest = { onAction(PlaylistDetailAction.DismissConnectPrompt) },
            onConnectClick = { onAction(PlaylistDetailAction.ConnectAudiusPressed) }
        )
    }

    if (playlistDetailState.isLoading) {
        LoadingScreen()
    } else {
        val context = LocalPlatformContext.current
        val toolbarOffsetHeightPx = remember { mutableStateOf(0f) }
        val showDeleteConfirm = remember { mutableStateOf(false) }
        val showEditDetails = remember { mutableStateOf(false) }

        val playbackState by musicPlayer.playbackState.collectAsState()
        val currentPlayingSongId = playbackState.currentMedia?.id

        val imageUrl = if (playlistDetailState.playlistIcon.isNotBlank()) {
            playlistDetailState.playlistIcon
        } else {
            playlistDetailState.songPlaylist.firstOrNull()?.songIconList?.songImageURL480px.orEmpty()
        }

        val painter = rememberAsyncImagePainter(
            model = remember(imageUrl, context) {
                ImageRequest.Builder(context = context)
                    .data(imageUrl).build()
            }
        )

        ConfirmDeletePlaylistDialog(
            playlistName = playlistDetailState.playlistName,
            openDialog = showDeleteConfirm,
            onConfirmDelete = { onAction(PlaylistDetailAction.DeletePlaylistClicked(playlistDetailState.playlistName)) }
        )

        EditPlaylistDialog(
            openDialog = showEditDetails,
            initialTitle = playlistDetailState.playlistName,
            onEditPlaylistClicked = { newName ->
                onAction(PlaylistDetailAction.EditPlaylistTitleClicked(playlistDetailState.playlistName, newName))
            }
        )

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
                    musicPlayer.setShuffleModeEnabled(true)
                    val randomSong = playlistDetailState.songPlaylist.randomOrNull()
                    if (randomSong != null) {
                        playMusic(musicPlayer, playlistDetailState.songPlaylist, randomSong.id)
                    }
                },
                onSongClicked = { onAction(PlaylistDetailAction.SongPressed(it)) },
                onFavoritePressed = { id, title, user, icon, isFav -> 
                    onAction(PlaylistDetailAction.FavoritePressed(id, title, user, icon, isFav)) 
                },
                onRepostPressed = { id, isRep ->
                    onAction(PlaylistDetailAction.RepostPressed(id, isRep))
                },
                onDeleteSong = { songId ->
                    onAction(PlaylistDetailAction.DeleteSongFromPlaylist(songId))
                },
                painter = painter,
                onArtistClicked = { id, name -> onAction(PlaylistDetailAction.ArtistClicked(id, name)) },
                currentPlayingSongId = currentPlayingSongId
            )
            AnimatedToolBar(
                playlistDetailState = playlistDetailState, 
                scrollState = toolbarOffsetHeightPx, 
                onBackButtonPressed = { onAction(PlaylistDetailAction.BackPressed(it)) }, 
                onDeletePlaylistClicked = { showDeleteConfirm.value = true },
                onEditPlaylistClicked = { showEditDetails.value = true }
            )
        }
    }
}

