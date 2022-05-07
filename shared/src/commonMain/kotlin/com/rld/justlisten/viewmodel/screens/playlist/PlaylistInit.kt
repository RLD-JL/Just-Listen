package com.rld.justlisten.viewmodel.screens.playlist

import com.rld.justlisten.Navigation
import com.rld.justlisten.ScreenParams
import com.rld.justlisten.datalayer.datacalls.playlist.getPlaylist
import com.rld.justlisten.datalayer.utils.Constants.list
import com.rld.justlisten.viewmodel.screens.ScreenInitSettings
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class PlaylistParams(val string: String) : ScreenParams

fun Navigation.initPlaylist(params: PlaylistParams) = ScreenInitSettings(
    title = "Playlist" + params.string,
    initState = { PlaylistState(isLoading = true) },
    callOnInit = {
        val playlist: Deferred<List<PlaylistItem>>
        val remix: Deferred<List<PlaylistItem>>
        val hot: Deferred<List<PlaylistItem>>
        var queryIndex = Random.nextInt(0, list.size)
        val queryIndex2 = Random.nextInt(0, list.size)
        if (queryIndex == queryIndex2) {
            if (queryIndex>0)
                queryIndex -= 1
            else queryIndex += 1
        }
        coroutineScope {
            playlist = async { dataRepository.getPlaylist(index = 20, PlayListEnum.TOP_PLAYLIST) }
            remix = async { dataRepository.getPlaylist(index = 20, PlayListEnum.REMIX, queryPlaylist = list[queryIndex]) }
            hot = async { dataRepository.getPlaylist(index = 20, PlayListEnum.HOT, queryPlaylist = list[queryIndex2]) }
        }
        stateManager.updateScreen(PlaylistState::class) {
            it.copy(
                remixPlaylist = remix.await(),
                isLoading = false,
                playlistItems = playlist.await(),
                hotPlaylist = hot.await(),
                queryIndex = queryIndex,
                queryIndex2 = queryIndex2
            )
        }
    },
    reinitOnEachNavigation = false
)

