package com.rld.justlisten.datalayer.datacalls.addplaylistscreen

import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.*

fun Repository.savePlaylist(playlistName: String, playlistDescription: String?) {
    localDb.savePlaylist(playlistName, playlistDescription)
}

fun Repository.getAddPlaylist(): List<AddPlaylist> {
    return localDb.getAddPlaylist()
}

fun Repository.updatePlaylistSongs(playlistName: String, playlistDescription: String?, songList: List<String>) {
    localDb.updatePlaylistSongs(playlistName, playlistDescription, songList)
}