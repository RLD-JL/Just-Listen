package com.example.justlisten.viewmodel.screens.addplaylist

import com.example.justlisten.Navigation
import com.example.justlisten.ScreenParams
import com.example.justlisten.datalayer.datacalls.addplaylistscreen.getAddPlaylist
import com.example.justlisten.viewmodel.screens.ScreenInitSettings
import com.example.justlisten.viewmodel.screens.library.LibraryState
import kotlinx.serialization.Serializable

@Serializable
data class AddPlaylistParams(val string: String) : ScreenParams

fun com.example.justlisten.Navigation.initAddPlaylist(params: AddPlaylistParams) = ScreenInitSettings(
    title = "Playlists" + params.string,
    initState = { AddPlaylistState(isLoading = true) },
    callOnInit = {
                 val playlist = dataRepository.getAddPlaylist()
        stateManager.updateScreen(AddPlaylistState::class) {
            it.copy(isLoading = false,
            playlistsCreated = playlist)
        }
    },
    reinitOnEachNavigation = true
)