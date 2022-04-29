package com.example.audius.android.ui.screenpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import com.example.audius.Navigation
import com.example.audius.ScreenIdentifier
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.addplaylistscreen.AddPlaylistScreen
import com.example.audius.android.ui.playlistscreen.PlaylistScreen
import com.example.audius.android.ui.libraryscreen.LibraryScreen
import com.example.audius.viewmodel.screens.playlist.*
import com.example.audius.viewmodel.screens.playlist.PlayListEnum.*
import com.example.audius.android.ui.playlistdetailscreen.PlaylistDetailScreen
import com.example.audius.android.ui.playlistdetailscreen.playMusicFromId
import com.example.audius.android.ui.searchscreen.SearchScreen
import com.example.audius.datalayer.models.UserModel
import com.example.audius.viewmodel.screens.Screen.*
import com.example.audius.viewmodel.screens.addplaylist.AddPlaylistParams
import com.example.audius.viewmodel.screens.addplaylist.addPlaylist
import com.example.audius.viewmodel.screens.addplaylist.updatePlaylist
import com.example.audius.viewmodel.screens.library.saveSongToFavorites
import com.example.audius.viewmodel.screens.library.saveSongToRecent
import com.example.audius.viewmodel.screens.playlistdetail.PlaylistDetailParams
import com.example.audius.viewmodel.screens.playlistdetail.PlaylistDetailState
import com.example.audius.viewmodel.screens.playlistdetail.saveDominantColor
import com.example.audius.viewmodel.screens.search.SearchScreenState
import com.example.audius.viewmodel.screens.search.saveSearchInfo
import com.example.audius.viewmodel.screens.search.searchFor
import com.example.audius.viewmodel.screens.search.updateSearch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Navigation.ScreenPicker(
    screenIdentifier: ScreenIdentifier,
    musicServiceConnection: MusicServiceConnection,
    dominantColor: (Int) -> Unit
) {
    val isPlayerReady: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }

    when (screenIdentifier.screen) {

        Library ->
            LibraryScreen(
                musicServiceConnection = musicServiceConnection,
                libraryState = stateProvider.get(screenIdentifier),
                onPlaylistPressed = { playlistId, playlistIcon, playlistTitle, playlistCreatedBy ->
                    navigate(
                        PlaylistDetail,
                        PlaylistDetailParams(
                            playlistId, playlistIcon, playlistTitle, playlistCreatedBy,
                            FAVORITE
                        )
                    )
                    events.playMusicFromPlaylist(playlistId = playlistId)
                },
                onPlayListViewClicked = { navigate(AddPlaylist, AddPlaylistParams("")) }
            )
        Playlist ->
            PlaylistScreen(
                lasItemReached = { lastIndex, playListEnum ->
                    when (playListEnum) {
                        TOP_PLAYLIST -> events.fetchPlaylist(lastIndex, TOP_PLAYLIST)
                        REMIX -> events.fetchPlaylist(lastIndex, REMIX)
                        HOT -> TODO()
                        CURRENT_PLAYLIST -> TODO()
                        FAVORITE -> TODO()
                    }
                },
                playlistState = stateProvider.get(screenIdentifier = screenIdentifier),
                onPlaylistClicked = { playlistId, playlistIcon, playlistTitle, playlistCreatedBy ->
                    navigate(
                        PlaylistDetail,
                        PlaylistDetailParams(
                            playlistId,
                            playlistIcon,
                            playlistTitle,
                            playlistCreatedBy,
                            CURRENT_PLAYLIST
                        )
                    )
                    events.playMusicFromPlaylist(playlistId = playlistId)
                },
                onSearchClicked = { navigate(Search) },
                refreshScreen = { events.refreshScreen() }
            )

        PlaylistDetail -> PlaylistDetailScreen(
            playlistDetailState = stateProvider.get(screenIdentifier = screenIdentifier),
            onBackButtonPressed = { onBackButtonPressed ->
                if (onBackButtonPressed) exitScreen()
            },
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
            dominantColor = { color ->
                events.saveDominantColor(color)
                dominantColor(color)
            },
            onSongPressed = { songId, title, userModel, songIconList ->

                playMusicFromId(
                    musicServiceConnection,
                    (stateProvider.get(screenIdentifier = screenIdentifier) as PlaylistDetailState).songPlaylist,
                    songId,
                    isPlayerReady.value,
                )

                isPlayerReady.value = true

                events.saveSongToRecent(songId, title, userModel, songIconList)
            }
        )
        Search -> SearchScreen(
            onBackPressed = {
                exitScreen()
            },
            onSearchPressed = { search ->
                events.saveSearchInfo(search)
                events.searchFor(search)
                isPlayerReady.value = false
            },
            searchScreenState = stateProvider.get(screenIdentifier = screenIdentifier),
            onSongPressed = { songId, title, user, songIcon ->
                playMusicFromId(
                    musicServiceConnection,
                    (stateProvider.get(screenIdentifier = screenIdentifier) as SearchScreenState).searchResultTracks,
                    songId,
                    isPlayerReady.value,
                )
                isPlayerReady.value = true

                events.saveSongToRecent(songId, title, UserModel(user), songIcon)
            },
            onPlaylistPressed = { playlistId, playlistIcon, playlistTitle, playlistCreatedBy ->
                navigate(
                    PlaylistDetail,
                    PlaylistDetailParams(
                        playlistId, playlistIcon, playlistTitle, playlistCreatedBy,
                        CURRENT_PLAYLIST
                    )
                )
                events.playMusicFromPlaylist(playlistId = playlistId)
            },
            onPreviousSearchedPressed = { searchText -> events.updateSearch(searchText) },
            updateSearch = { searchText -> events.updateSearch(searchText) }
        )
        AddPlaylist -> AddPlaylistScreen(addPlaylistState = stateProvider.get(screenIdentifier),
            onAddPlaylistClicked = { playlistName, playlistDescription ->
                events.addPlaylist(playlistName, playlistDescription)
                events.updatePlaylist()
            },
            clickedToAddSongToPlaylist = { _, _, _ -> }
        )
    }
}

fun updateFavorite(
    isFavorite: Boolean,
    musicServiceConnection: MusicServiceConnection,
    songId: String
) {
    musicServiceConnection.updateFavorite(songId, isFavorite)
    musicServiceConnection.isFavorite[songId] = isFavorite
}
