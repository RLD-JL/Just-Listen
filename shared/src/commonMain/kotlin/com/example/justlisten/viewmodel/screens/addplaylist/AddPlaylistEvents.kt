package com.example.justlisten.viewmodel.screens.addplaylist

import com.example.justlisten.datalayer.datacalls.addplaylistscreen.getAddPlaylist
import com.example.justlisten.datalayer.datacalls.addplaylistscreen.savePlaylist
import com.example.justlisten.datalayer.datacalls.addplaylistscreen.updatePlaylistSongs
import com.example.justlisten.datalayer.localdb.addplaylistscreen.AddPlaylist
import com.example.justlisten.viewmodel.Events

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

fun Events.getPlaylist(): List<AddPlaylist> {
    return dataRepository.getAddPlaylist()
}

fun Events.updatePlaylistSongs(
    playlistName: String,
    playlistDescription: String?,
    songList: List<String>
) = dataRepository.updatePlaylistSongs(playlistName,playlistDescription, songList)