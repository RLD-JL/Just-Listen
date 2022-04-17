package com.example.audius.datalayer.datacalls.addplaylistscreen

import com.example.audius.datalayer.Repository
import com.example.audius.datalayer.localdb.addplaylistscreen.AddPlaylist
import com.example.audius.datalayer.localdb.addplaylistscreen.getAddPlaylist
import com.example.audius.datalayer.localdb.addplaylistscreen.savePlaylist

fun Repository.savePlaylist(playlistName: String, playlistDescription: String) {
    localDb.savePlaylist(playlistName, playlistDescription)
}

fun Repository.getAddPlaylist(): List<AddPlaylist> {
    return localDb.getAddPlaylist()
}
