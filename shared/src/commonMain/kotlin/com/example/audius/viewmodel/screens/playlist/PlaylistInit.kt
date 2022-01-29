package com.example.audius.viewmodel.screens.playlist

import com.example.audius.Navigation
import com.example.audius.ScreenParams
import com.example.audius.datalayer.datacalls.playlist.getPlaylist
import com.example.audius.viewmodel.screens.ScreenInitSettings
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistParams(val string: String) : ScreenParams

fun Navigation.initPlaylist(params: PlaylistParams) = ScreenInitSettings(
    title = "Playlist" + params.string,
    initState = { PlaylistState(isLoading = true) },
    callOnInit = {
        val playlist: Deferred<List<PlaylistItem>>
        val remix: Deferred<List<PlaylistItem>>
        coroutineScope {
             playlist = async { dataRepository.getPlaylist(index = 20, PlayListEnum.TOP_PLAYLIST) }
             remix = async { dataRepository.getPlaylist(index = 20, PlayListEnum.REMIX) }
        }
        stateManager.updateScreen(PlaylistState::class) {
            it.copy(
                remixPlaylist = remix.await(),
                isLoading = false,
                playlistItems =playlist.await()
            )
        }
    },
    reinitOnEachNavigation = false
)

