package com.rld.justlisten.viewmodel.screens.library

import com.rld.justlisten.Navigation
import com.rld.justlisten.ScreenParams
import com.rld.justlisten.datalayer.datacalls.library.getFavoritePlaylist
import com.rld.justlisten.datalayer.datacalls.library.getRecentSongs
import com.rld.justlisten.viewmodel.screens.ScreenInitSettings
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import kotlinx.serialization.Serializable

@Serializable
data class LibraryParams(val string: String) : ScreenParams

fun Navigation.initLibrary(params: LibraryParams) = ScreenInitSettings(
    title = "Library" + params.string,
    initState = { LibraryState(isLoading = true) },
    callOnInit = {
        val recentSongs = dataRepository.getRecentSongs(20).map { playlistModel ->
            PlaylistItem(playlistModel, playlistModel.isFavorite)
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