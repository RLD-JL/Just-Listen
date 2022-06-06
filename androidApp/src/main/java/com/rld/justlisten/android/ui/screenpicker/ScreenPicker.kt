package com.rld.justlisten.android.ui.screenpicker

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import com.rld.justlisten.Navigation
import com.rld.justlisten.ScreenIdentifier
import com.rld.justlisten.android.exoplayer.MusicService
import com.rld.justlisten.android.exoplayer.MusicServiceConnection
import com.rld.justlisten.android.exoplayer.library.extension.displayIconUri
import com.rld.justlisten.android.exoplayer.library.extension.id
import com.rld.justlisten.android.exoplayer.library.extension.title
import com.rld.justlisten.android.ui.addplaylistscreen.AddPlaylistScreen
import com.rld.justlisten.android.ui.donationscreen.DonationScreen
import com.rld.justlisten.android.ui.libraryscreen.LibraryScreen
import com.rld.justlisten.android.ui.playlistdetailscreen.PlaylistDetailScreen
import com.rld.justlisten.android.ui.playlistdetailscreen.playMusicFromId
import com.rld.justlisten.android.ui.playlistscreen.PlaylistScreen
import com.rld.justlisten.android.ui.searchscreen.SearchScreen
import com.rld.justlisten.android.ui.settingsscreen.SettingsScreen
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.screens.Screen.*
import com.rld.justlisten.viewmodel.screens.addplaylist.AddPlaylistParams
import com.rld.justlisten.viewmodel.screens.addplaylist.addPlaylist
import com.rld.justlisten.viewmodel.screens.addplaylist.updatePlaylist
import com.rld.justlisten.viewmodel.screens.library.getLastPlayed
import com.rld.justlisten.viewmodel.screens.library.saveSongToFavorites
import com.rld.justlisten.viewmodel.screens.library.saveSongToMostPlayed
import com.rld.justlisten.viewmodel.screens.library.saveSongToRecent
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum.*
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistState
import com.rld.justlisten.viewmodel.screens.playlist.playMusicFromPlaylist
import com.rld.justlisten.viewmodel.screens.playlist.refreshScreen
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailParams
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState
import com.rld.justlisten.viewmodel.screens.search.SearchScreenState
import com.rld.justlisten.viewmodel.screens.search.saveSearchInfo
import com.rld.justlisten.viewmodel.screens.search.searchFor
import com.rld.justlisten.viewmodel.screens.settings.saveSettingsInfo
import com.rld.justlisten.viewmodel.screens.settings.updateScreen

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Navigation.ScreenPicker(
    screenIdentifier: ScreenIdentifier,
    musicServiceConnection: MusicServiceConnection,
    settingsUpdated: () -> Unit
) {
    val isPlayerReady: MutableState<Boolean> = rememberSaveable{
        mutableStateOf(false)
    }

    val currentId = remember { musicServiceConnection.currentPlayingSong.value?.id }
    val updateRecentSong =
        remember { derivedStateOf { currentId != musicServiceConnection.currentPlayingSong.value?.id } }
    if (updateRecentSong.value) {
        LaunchedEffect(musicServiceConnection.currentPlayingSong.value?.id) {
            val title = musicServiceConnection.currentPlayingSong.value?.title ?: "title"
            val newId = musicServiceConnection.currentPlayingSong.value?.id ?: "id"
            val user = UserModel("asd")
            val songIcon =
                musicServiceConnection.currentPlayingSong.value?.displayIconUri.toString()
            val icon = SongIconList(
                songImageURL150px = songIcon,
                songImageURL480px = songIcon,
                songImageURL1000px = songIcon.replace("480", "1000")
            )
            events.saveSongToRecent(newId, title, user, icon)
        }
    }

    if (MusicService.songHasRepeated.value) {
        LaunchedEffect(key1 = MusicService.songHasRepeated) {
            val title = musicServiceConnection.currentPlayingSong.value?.title ?: "title"
            val newId = musicServiceConnection.currentPlayingSong.value?.id ?: "id"
            val user = UserModel("asd")
            val songIcon =
                musicServiceConnection.currentPlayingSong.value?.displayIconUri.toString()
            val icon = SongIconList(
                songImageURL150px = songIcon,
                songImageURL480px = songIcon,
                songImageURL1000px = songIcon.replace("480", "1000")
            )
            events.saveSongToMostPlayed(newId, title, user, icon)
            MusicService.songHasRepeated.value = false
        }
    }

    when (screenIdentifier.screen) {

        Library ->
            LibraryScreen(
                musicServiceConnection = musicServiceConnection,
                libraryState = stateProvider.get(screenIdentifier),
                onFavoritePlaylistPressed = { playlistId, playlistIcon, playlistTitle, playlistCreatedBy ->
                    navigate(
                        PlaylistDetail,
                        PlaylistDetailParams(
                            playlistId, playlistIcon, playlistTitle, playlistCreatedBy,
                            FAVORITE
                        )
                    )
                    events.playMusicFromPlaylist(playlistId = playlistId)
                },
                onMostPlaylistPressed = {
                        playlistId, playlistIcon, playlistTitle, playlistCreatedBy ->
                    navigate(
                        PlaylistDetail,
                        PlaylistDetailParams(
                            playlistId, playlistIcon, playlistTitle, playlistCreatedBy,
                            MOST_PLAYED
                        )
                    )
                    events.playMusicFromPlaylist(playlistId = playlistId)
                },
                onPlayListViewClicked = { navigate(AddPlaylist, AddPlaylistParams("")) },
                lasItemReached = { index ->
                    events.getLastPlayed(index.toLong())
                },
            )
        Playlist ->
            PlaylistScreen(
                events = events,
                playlistState = stateProvider.get(screenIdentifier = screenIdentifier),
                onPlaylistClicked = { playlistId, playlistIcon, playlistTitle, playlistCreatedBy, _ ->
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
                refreshScreen = { events.refreshScreen() },
                onSongPressed = { songId, _, _, _ ->
                    if (isPlayerReady.value) {
                        isPlayerReady.value = false
                    }
                    playMusicFromId(
                        musicServiceConnection,
                        (stateProvider.get(screenIdentifier = screenIdentifier) as PlaylistState).tracksList,
                        songId,
                        isPlayerReady.value,
                    )
                    isPlayerReady.value = true
                }
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
            onSongPressed = { songId ->

                playMusicFromId(
                    musicServiceConnection,
                    (stateProvider.get(screenIdentifier = screenIdentifier) as PlaylistDetailState).songPlaylist,
                    songId,
                    isPlayerReady.value,
                )
                isPlayerReady.value = true
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
            onSongPressed = { songId, _, _, _ ->
                playMusicFromId(
                    musicServiceConnection,
                    (stateProvider.get(screenIdentifier = screenIdentifier) as SearchScreenState).searchResultTracks,
                    songId,
                    isPlayerReady.value,
                )
                isPlayerReady.value = true
            },
            onPlaylistPressed = { playlistId, playlistIcon, playlistTitle, playlistCreatedBy, _ ->
                navigate(
                    PlaylistDetail,
                    PlaylistDetailParams(
                        playlistId, playlistIcon, playlistTitle, playlistCreatedBy,
                        CURRENT_PLAYLIST
                    )
                )
                events.playMusicFromPlaylist(playlistId = playlistId)
            }
        )
        AddPlaylist -> AddPlaylistScreen(addPlaylistState = stateProvider.get(screenIdentifier),
            onAddPlaylistClicked = { playlistName, playlistDescription ->
                events.addPlaylist(playlistName, playlistDescription)
                events.updatePlaylist()
            },
            clickedToAddSongToPlaylist = { playlistTitle, _, songsList ->
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
        Donation -> DonationScreen()
        Settings -> SettingsScreen(settings = stateProvider.get(screenIdentifier),
            updateSettings = { settingsState ->
                events.saveSettingsInfo(
                    settingsState.hasDonationNavigationOn,
                    settingsState.isDarkThemeOn,
                    settingsState.palletColor
                )
                events.updateScreen()
                settingsUpdated()
            })
    }
}

fun updateFavorite(
    isFavorite: Boolean,
    musicServiceConnection: MusicServiceConnection,
    songId: String
) {
    musicServiceConnection.isFavorite[songId] = isFavorite
}
