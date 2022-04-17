package com.example.audius.viewmodel.screens.addplaylist

import com.example.audius.Navigation
import com.example.audius.ScreenParams
import com.example.audius.datalayer.datacalls.addplaylistscreen.getAddPlaylist
import com.example.audius.viewmodel.screens.ScreenInitSettings
import com.example.audius.viewmodel.screens.library.LibraryState
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