package com.example.audius.android.ui.screenpicker

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import com.example.audius.Navigation
import com.example.audius.ScreenIdentifier
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.ui.playlistscreen.components.SpotifyHome
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
                onPlaylistClicked = {playlistId, playlistIcon, playlistTitle, playlistCreatedBy->
                         navigate(Screen.PlaylistDetail, PlaylistDetailParams(playlistId, playlistIcon,  playlistTitle, playlistCreatedBy))
                         events.playMusicFromPlaylist(playlistId = playlistId)}
            )

        Screen.PlaylistDetail -> SpotifyDetailScreen(
            playlistDetailState = (stateProvider.get(screenIdentifier = screenIdentifier) as PlaylistDetailState),
        )
    }
}

