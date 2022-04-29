package com.example.audius.datalayer.datacalls.addplaylistscreen

import com.example.audius.datalayer.Repository
import com.example.audius.datalayer.localdb.addplaylistscreen.*

fun Repository.savePlaylist(playlistName: String, playlistDescription: String?) {
    localDb.savePlaylist(playlistName, playlistDescription)
}

fun Repository.getAddPlaylist(): List<AddPlaylist> {
    return localDb.getAddPlaylist()
}

fun Repository.updatePlaylistSongs(playlistName: String, playlistDescription: String?, songList: List<String>) {
    localDb.updatePlaylistSongs(playlistName, playlistDescription, songList)
}