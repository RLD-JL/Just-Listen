package com.rld.justlisten.viewmodel.screens.addplaylist

import com.rld.justlisten.Navigation
import com.rld.justlisten.ScreenParams
import com.rld.justlisten.datalayer.datacalls.addplaylistscreen.getAddPlaylist
import com.rld.justlisten.viewmodel.screens.ScreenInitSettings
import kotlinx.serialization.Serializable

@Serializable
data class AddPlaylistParams(val string: String) : ScreenParams

fun Navigation.initAddPlaylist(params: AddPlaylistParams) = ScreenInitSettings(
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