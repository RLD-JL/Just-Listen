package com.example.audius.viewmodel.screens.library

import com.example.audius.Navigation
import com.example.audius.ScreenParams
import com.example.audius.datalayer.datacalls.library.getFavoritePlaylist
import com.example.audius.datalayer.datacalls.library.getRecentSongs
import com.example.audius.viewmodel.screens.ScreenInitSettings
import com.example.audius.viewmodel.screens.playlist.PlaylistItem
import kotlinx.serialization.Serializable

@Serializable
data class LibraryParams(val string: String) : ScreenParams

fun Navigation.initLibrary(params: LibraryParams) = ScreenInitSettings(
    title = "Library" + params.string,
    initState = { LibraryState(isLoading = true) },
    callOnInit = {
        val recentSongs = dataRepository.getRecentSongs().map { playlistModel ->
            PlaylistItem(playlistModel)
        }.toList()
        val favoritePlaylist = dataRepository.getFavoritePlaylist().map { playlistModel ->
            PlaylistItem(playlistModel)
        }.toList()
        stateManager.updateScreen(LibraryState::class) {
            it.copy(isLoading = false, favoritePlaylistItems = favoritePlaylist,
            recentSongsItems = recentSongs)
        }
    },
    reinitOnEachNavigation = true
)