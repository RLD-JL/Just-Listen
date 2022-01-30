package com.example.audius.viewmodel.screens.playlist

import com.example.audius.datalayer.datacalls.playlist.getPlaylist
import com.example.audius.viewmodel.Events
import com.example.audius.viewmodel.screens.playlist.PlayListEnum.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

fun Events.fetchPlaylist(index: Int, playlistEnum: PlayListEnum) = screenCoroutine {
    stateManager.updateScreen(PlaylistState::class) {
        when(playlistEnum) {
            TOP_PLAYLIST ->  it.copy(playlistItems = dataRepository.getPlaylist(index, playlistEnum))
            REMIX ->  it.copy(remixPlaylist = dataRepository.getPlaylist(index, playlistEnum))
            HOT -> TODO()
            CURRENT_PLAYLIST -> TODO()
            FAVORITE -> it.copy(currentPlaylist = dataRepository.getPlaylist(0,playlistEnum))
        }
    }
}

fun Events.playMusicFromPlaylist(playlistId: String) {
    currentPlaylistId = playlistId
}

fun Events.refreshScreen() = screenCoroutine {
    stateManager.updateScreen(PlaylistState::class) {
        it.copy(isLoading = true)
    }
    stateManager.updateScreen(PlaylistState::class) {
        val playlist: Deferred<List<PlaylistItem>>
        val remix: Deferred<List<PlaylistItem>>
        coroutineScope {
            playlist = async { dataRepository.getPlaylist(index = 20, TOP_PLAYLIST) }
            remix = async { dataRepository.getPlaylist(index = 20, REMIX) }
        }
            it.copy(
                remixPlaylist = remix.await().shuffled(),
                isLoading = false,
                playlistItems =playlist.await().shuffled()
            )
    }

}

