package com.example.justlisten.viewmodel.screens.library

import com.example.justlisten.Navigation
import com.example.justlisten.ScreenParams
import com.example.justlisten.datalayer.datacalls.library.getFavoritePlaylist
import com.example.justlisten.datalayer.datacalls.library.getRecentSongs
import com.example.justlisten.viewmodel.screens.ScreenInitSettings
import com.example.justlisten.viewmodel.screens.playlist.PlaylistItem
import kotlinx.serialization.Serializable

@Serializable
data class LibraryParams(val string: String) : ScreenParams

fun com.example.justlisten.Navigation.initLibrary(params: LibraryParams) = ScreenInitSettings(
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