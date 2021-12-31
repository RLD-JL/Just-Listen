package com.example.audius.android.ui.screenpicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import com.example.audius.Navigation
import com.example.audius.ScreenIdentifier
import com.example.audius.ScreenState
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.playlistscreen.PlaylistScreen
import com.example.audius.android.ui.trendinglistscreen.TrendingListScreen
import com.example.audius.viewmodel.screens.playlist.*
import com.example.audius.viewmodel.screens.playlist.PlayListEnum.*
import com.example.audius.android.ui.playlistdetailscreen.PlaylistDetailScreen
import com.example.audius.android.ui.playlistdetailscreen.playMusicFromId
import com.example.audius.android.ui.searchscreen.SearchScreen
import com.example.audius.viewmodel.screens.Screen.*
import com.example.audius.viewmodel.screens.playlistdetail.PlaylistDetailParams
import com.example.audius.viewmodel.screens.search.SearchInitParams
import com.example.audius.viewmodel.screens.search.SearchScreenState
import com.example.audius.viewmodel.screens.search.saveSearchInfo
import com.example.audius.viewmodel.screens.search.searchFor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Navigation.ScreenPicker(
    screenIdentifier: ScreenIdentifier,
    musicServiceConnection: MusicServiceConnection
) {
    val isPlayerReady: MutableState<Boolean> = remember {
        mutableStateOf(false)
    }

    when (screenIdentifier.screen) {

        TrendingList ->
            TrendingListScreen(
                musicServiceConnection = musicServiceConnection,
                trendingListState = stateProvider.get(screenIdentifier),
                onLastItemClick = { songId, songIcon ->
                    events.playMusic(songId = songId, songIcon = songIcon)
                },
                onSkipNextPressed = { events.skipToNextSong() }
            )
        Playlist ->
            PlaylistScreen(
                lasItemReached = {lastIndex, playListEnum ->
                    when(playListEnum) {
                        TOP_PLAYLIST ->events.fetchPlaylist(lastIndex, TOP_PLAYLIST)
                        REMIX -> events.fetchPlaylist(lastIndex, REMIX)
                        HOT -> TODO()
                        CURRENT_PLAYLIST -> TODO()
                    }
                },
                playlistState = stateProvider.get(screenIdentifier = screenIdentifier),
                onPlaylistClicked = {playlistId, playlistIcon, playlistTitle, playlistCreatedBy->
                         navigate(PlaylistDetail, PlaylistDetailParams(playlistId, playlistIcon,  playlistTitle, playlistCreatedBy))
                         events.playMusicFromPlaylist(playlistId = playlistId)},
                onSearchClicked = {navigate(Search, SearchInitParams("yolo"))}
            )

        PlaylistDetail -> PlaylistDetailScreen(
            playlistDetailState = stateProvider.get(screenIdentifier = screenIdentifier),
            onBackButtonPressed = {onBackButtonPressed ->
                if (onBackButtonPressed) exitScreen()
            },
            musicServiceConnection = musicServiceConnection)
        Search -> SearchScreen(
            onBackPressed = {
                exitScreen()
            },
            onSearchPressed = {search ->
                events.saveSearchInfo(search)
                events.searchFor(search)
                isPlayerReady.value = false
            },
            searchScreenState =  stateProvider.get(screenIdentifier = screenIdentifier),
            onSongPressed = { songId ->
                playMusicFromId(
                    musicServiceConnection,
                    (stateProvider.get(screenIdentifier = screenIdentifier) as SearchScreenState).searchResultTracks,
                    songId,
                    isPlayerReady.value,
                )
                isPlayerReady.value = true
            }
        )
    }
}

