package com.example.audius.android.ui.screenpicker

import android.support.v4.media.MediaBrowserCompat
import androidx.compose.runtime.Composable
import com.example.audius.Navigation
import com.example.audius.ScreenIdentifier
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.exoplayer.utils.Constants.MEDIA_ROOT_ID
import com.example.audius.android.ui.playlistscreen.components.SpotifyHome
import com.example.audius.android.ui.test.Album
import com.example.audius.android.ui.test.AlbumsDataProvider
import com.example.audius.android.ui.trendinglistscreen.TrendingListScreen
import com.example.audius.viewmodel.screens.Screen
import com.example.audius.viewmodel.screens.trending.*
import com.example.audius.viewmodel.screens.trending.PlayListEnum.*
import com.guru.composecookbook.spotify.ui.details.components.SpotifyDetailScreen

@Composable
fun Navigation.ScreenPicker(
    screenIdentifier: ScreenIdentifier,
    musicServiceConnection: MusicServiceConnection
) {

    when (screenIdentifier.screen) {

        Screen.TrendingList ->
            TrendingListScreen(
                musicServiceConnection = musicServiceConnection,
                trendingListState = stateProvider.get(screenIdentifier),
                onLastItemClick = { songId, songIcon ->
                    events.playMusic(songId = songId, songIcon = songIcon)
                },
                onSkipNextPressed = { events.skipToNextSong() }
            )
        Screen.Playlist ->
            SpotifyHome(
                lasItemReached = {lastIndex, playListEnum ->
                    when(playListEnum) {
                        TOP_PLAYLIST ->events.fetchPlaylist(lastIndex, TOP_PLAYLIST)
                        REMIX -> events.fetchPlaylist(lastIndex, REMIX)
                        HOT -> TODO()
                        CURRENT_PLAYLIST -> TODO()
                    }
                },
                playlistState = stateProvider.get(screenIdentifier = screenIdentifier),
                musicServiceConnection = musicServiceConnection,
                onPlaylistClicked = {playlistId->
                         navigate(Screen.PlaylistDetail, PlaylistDetailParams("yolo"))
                         events.playMusicFromPlaylist(playlistId = playlistId)}
            )
        Screen.PlaylistDetail -> SpotifyDetailScreen(album = AlbumsDataProvider.album,
            playlistDetailState = stateProvider.get(screenIdentifier = screenIdentifier))
    }
}

