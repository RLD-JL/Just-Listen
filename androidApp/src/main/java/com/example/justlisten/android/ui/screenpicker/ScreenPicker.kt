package com.example.justlisten.android.ui.screenpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import com.example.justlisten.Navigation
import com.example.justlisten.ScreenIdentifier
import com.example.justlisten.android.exoplayer.MusicServiceConnection
import com.example.justlisten.android.ui.addplaylistscreen.AddPlaylistScreen
import com.example.justlisten.android.ui.playlistscreen.PlaylistScreen
import com.example.justlisten.android.ui.libraryscreen.LibraryScreen
import com.example.justlisten.viewmodel.screens.playlist.*
import com.example.justlisten.viewmodel.screens.playlist.PlayListEnum.*
import com.example.justlisten.android.ui.playlistdetailscreen.PlaylistDetailScreen
import com.example.justlisten.android.ui.playlistdetailscreen.playMusicFromId
import com.example.justlisten.android.ui.searchscreen.SearchScreen
import com.example.justlisten.datalayer.models.UserModel
import com.example.justlisten.viewmodel.screens.Screen.*
import com.example.justlisten.viewmodel.screens.addplaylist.AddPlaylistParams
import com.example.justlisten.viewmodel.screens.addplaylist.addPlaylist
import com.example.justlisten.viewmodel.screens.addplaylist.updatePlaylist
import com.example.justlisten.viewmodel.screens.library.saveSongToFavorites
import com.example.justlisten.viewmodel.screens.library.saveSongToRecent
import com.example.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailParams
import com.example.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState
import com.example.justlisten.viewmodel.screens.playlistdetail.saveDominantColor
import com.example.justlisten.viewmodel.screens.search.SearchScreenState
import com.example.justlisten.viewmodel.screens.search.saveSearchInfo
import com.example.justlisten.viewmodel.screens.search.searchFor
import com.example.justlisten.viewmodel.screens.search.updateSearch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun com.example.justlisten.Navigation.ScreenPicker(
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
                        CREATED_BY_USER -> TODO()
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
            clickedToAddSongToPlaylist = { playlistTitle, _,songsList ->
                val playlistId = ""
                val playlistIcon = ""
                val playlistCreatedBy = "ME"
                navigate(
                    PlaylistDetail,
                    PlaylistDetailParams(
                        playlistId, playlistIcon, playlistTitle, playlistCreatedBy,
                        CREATED_BY_USER, songsList
                    )
                )
                events.playMusicFromPlaylist(playlistId = playlistId)
            }
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
