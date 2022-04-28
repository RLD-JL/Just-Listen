package com.example.audius.viewmodel.screens.addplaylist

import com.example.audius.datalayer.datacalls.addplaylistscreen.getAddPlaylist
import com.example.audius.datalayer.datacalls.addplaylistscreen.savePlaylist
import com.example.audius.viewmodel.Events
import com.example.audius.viewmodel.screens.search.SearchScreenState

fun Events.addPlaylist(
    playlistName: String,
    playlistDescription: String?
) = dataRepository.savePlaylist(playlistName, playlistDescription)

fun Events.updatePlaylist() = screenCoroutine {
    val playlist = dataRepository.getAddPlaylist()
    stateManager.updateScreen(AddPlaylistState::class) {
        it.copy(playlistsCreated = playlist)

    }
}