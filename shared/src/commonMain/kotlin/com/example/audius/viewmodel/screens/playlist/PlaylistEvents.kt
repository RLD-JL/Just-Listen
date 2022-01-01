package com.example.audius.viewmodel.screens.playlist

import com.example.audius.datalayer.datacalls.playlist.getPlaylist
import com.example.audius.viewmodel.Events
import com.example.audius.viewmodel.screens.playlist.PlayListEnum.*

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

