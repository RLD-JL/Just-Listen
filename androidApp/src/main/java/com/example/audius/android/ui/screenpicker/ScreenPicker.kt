package com.example.audius.android.ui.screenpicker
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import com.example.audius.Navigation
import com.example.audius.ScreenIdentifier
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.playlistscreen.PlaylistScreen
import com.example.audius.android.ui.trendinglistscreen.TrendingListScreen
import com.example.audius.viewmodel.screens.playlist.*
import com.example.audius.viewmodel.screens.playlist.PlayListEnum.*
import com.example.audius.android.ui.playlistdetailscreen.PlaylistDetailScreen
import com.example.audius.android.ui.searchscreen.SearchScreen
import com.example.audius.viewmodel.screens.Screen.*
import com.example.audius.viewmodel.screens.playlistdetail.PlaylistDetailParams
import com.example.audius.viewmodel.screens.search.SearchInitParams
import com.example.audius.viewmodel.screens.search.saveSearchInfo
import com.example.audius.viewmodel.screens.search.searchFor

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun Navigation.ScreenPicker(
    screenIdentifier: ScreenIdentifier,
    musicServiceConnection: MusicServiceConnection
) {
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
            },
            searchScreenState =  stateProvider.get(screenIdentifier = screenIdentifier)
        )
    }
}

