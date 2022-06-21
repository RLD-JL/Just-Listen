package com.rld.justlisten.android.ui

import android.media.session.PlaybackState
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import com.rld.justlisten.Navigation
import com.rld.justlisten.android.exoplayer.MusicServiceConnection
import com.rld.justlisten.android.exoplayer.library.extension.id
import com.rld.justlisten.android.ui.bottombars.bottombarnav.Level1BottomBar
import com.rld.justlisten.android.ui.bottombars.playbar.PlayerBarSheetContent
import com.rld.justlisten.android.ui.extensions.fraction
import com.rld.justlisten.android.ui.screenpicker.ScreenPicker
import com.rld.justlisten.android.ui.screenpicker.updateFavorite
import com.rld.justlisten.android.ui.utils.lerp
import com.rld.justlisten.viewmodel.screens.addplaylist.addPlaylist
import com.rld.justlisten.viewmodel.screens.addplaylist.getPlaylist
import com.rld.justlisten.viewmodel.screens.addplaylist.updatePlaylistSongs
import com.rld.justlisten.viewmodel.screens.library.saveSongToFavorites
import kotlinx.coroutines.launch

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun Navigation.OnePane(
    saveableStateHolder: SaveableStateHolder,
    musicServiceConnection: MusicServiceConnection,
    settingsUpdated: () -> Unit,
    hasNavigationFundOn: Boolean,
    updateStatusBarColor: (Int, Boolean) -> Unit
) {
    val shouldHavePlayBar by remember {
        derivedStateOf {
            musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PLAYING
                    || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_PAUSED
                    || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_SKIPPING_TO_NEXT
                    || musicServiceConnection.playbackState.value?.state == PlaybackState.STATE_BUFFERING
                    || musicServiceConnection.currentPlayingSong.value != null
        }
    }

        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
        )

        val context = LocalContext.current

        val addPlaylistList = remember { mutableStateOf(events.getPlaylist()) }

        val coroutineScope = rememberCoroutineScope()
    if (scaffoldState.bottomSheetState.isExpanded) {
        BackHandler {
            coroutineScope.launch {
                scaffoldState.bottomSheetState.collapse()
            }
        }
    }
        Scaffold(
            bottomBar = {
                if (currentScreenIdentifier.screen.navigationLevel == 1) {
                    Level1BottomBar(
                        currentScreenIdentifier,
                        Modifier.offset(y = lerp(0f, 65f, scaffoldState.fraction).dp),
                        hasNavigationFundOn
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
                            isExtended = scaffoldState.bottomSheetState.isExpanded,
                            onSkipNextPressed = { musicServiceConnection.transportControls.skipToNext() },
                            musicServiceConnection = musicServiceConnection,
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
                            },
                            clickedToAddSongToPlaylist = { playlistTitle, playlistDescription, songList ->
                                val list = songList.toMutableList()
                                list.add(musicServiceConnection.currentPlayingSong.value?.id ?: "")
                                Toast.makeText(
                                    context,
                                    "The song was added to $playlistTitle",
                                    Toast.LENGTH_SHORT
                                ).show()
                                events.updatePlaylistSongs(
                                    playlistTitle,
                                    playlistDescription,
                                    list
                                )
                            },
                            newDominantColor = { color ->
                                updateStatusBarColor(
                                    color,
                                    scaffoldState.bottomSheetState.isExpanded &&
                                            scaffoldState.bottomSheetState.targetValue != BottomSheetValue.Collapsed
                                )
                            },
                            playBarMinimizedClicked = {
                                coroutineScope.launch { scaffoldState.bottomSheetState.expand() }
                            },
                            events = events
                        )
                    }, content = {
                        Column(
                            modifier = if (shouldHavePlayBar) Modifier.padding(bottom = bottomBarPadding + 55.dp) else
                                Modifier.padding(bottom = bottomBarPadding)
                        ) {
                            saveableStateHolder.SaveableStateProvider(currentScreenIdentifier.URI) {
                                ScreenPicker(
                                    currentScreenIdentifier,
                                    musicServiceConnection,
                                    settingsUpdated = settingsUpdated
                                )
                            }
                        }
                    }, sheetPeekHeight = if (shouldHavePlayBar) {
                        bottomBarPadding + 65.dp
                    } else bottomBarPadding - 50.dp
                )
            })
    }