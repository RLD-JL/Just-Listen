package com.example.audius.android.ui

import android.media.session.PlaybackState
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.example.audius.Navigation
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.bottombars.Level1BottomBar
import com.example.audius.android.ui.bottombars.playbar.PlayerBarSheetContent
import com.example.audius.android.ui.extensions.fraction
import com.example.audius.android.ui.screenpicker.ScreenPicker
import com.example.audius.android.ui.screenpicker.updateFavorite
import com.example.audius.android.ui.utils.lerp
import com.example.audius.viewmodel.screens.addplaylist.addPlaylist
import com.example.audius.viewmodel.screens.addplaylist.getPlaylist
import com.example.audius.viewmodel.screens.library.saveSongToFavorites
import kotlinx.coroutines.launch

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun Navigation.OnePane(
    saveableStateHolder: SaveableStateHolder,
    musicServiceConnection: MusicServiceConnection
) {
    val shouldHavePlayBar =
        musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PLAYING
                || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PAUSED
                || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_SKIPPING_TO_NEXT
                || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_BUFFERING
                || musicServiceConnection.currentPlayingSong.value != null

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )

    val dominantColorMutable = remember { mutableStateOf(12312312) }

    val addPlaylistList = remember { mutableStateOf(events.getPlaylist()) }

    val coroutineScope = rememberCoroutineScope()
    Scaffold(
        bottomBar = {
            if (currentScreenIdentifier.screen.navigationLevel == 1) {
                Level1BottomBar(
                    currentScreenIdentifier,
                    Modifier.offset(y = lerp(0f, 65f, scaffoldState.fraction).dp)
                )
            }
        },
        content = {
            val bottomBarPadding = it.calculateBottomPadding()
            BottomSheetScaffold(
                modifier = Modifier.fillMaxSize(),
                scaffoldState = scaffoldState,
                sheetContent = {
                    PlayerBarSheetContent(
                        onCollapsedClicked = { coroutineScope.launch { scaffoldState.bottomSheetState.collapse() } },
                        bottomPadding = bottomBarPadding,
                        currentFraction = scaffoldState.fraction,
                        onSkipNextPressed = { musicServiceConnection.transportControls.skipToNext() },
                        musicServiceConnection = musicServiceConnection,
                        dominantColor = dominantColorMutable.value,
                        onFavoritePressed = { id, title, userModel, songIconList, isFavorite ->
                            events.saveSongToFavorites(
                                id,
                                title,
                                userModel,
                                songIconList,
                                isFavorite = isFavorite
                            )
                            updateFavorite(isFavorite, musicServiceConnection, id)
                        },
                        addPlaylistList = addPlaylistList.value,
                        onAddPlaylistClicked = { playlistName, playlistDescription ->
                            events.addPlaylist(playlistName, playlistDescription)
                            addPlaylistList.value = events.getPlaylist()
                        },
                        getLatestPlaylist = {
                            addPlaylistList.value = events.getPlaylist()
                        }
                    )
                }, content = {
                    Column(
                        modifier = if (shouldHavePlayBar) Modifier.padding(bottom = bottomBarPadding + 55.dp) else
                            Modifier.padding(bottom = bottomBarPadding)
                    ) {
                        saveableStateHolder.SaveableStateProvider(currentScreenIdentifier.URI) {
                            ScreenPicker(currentScreenIdentifier, musicServiceConnection,
                                dominantColor = { dominantColorMutable.value = it })
                        }
                    }
                }, sheetPeekHeight = if (shouldHavePlayBar) {
                    bottomBarPadding + 65.dp
                } else bottomBarPadding - 50.dp
            )
        })
}