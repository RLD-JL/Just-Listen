package com.example.audius.datalayer.localdb.addplaylistscreen

import com.example.audius.viewmodel.screens.playlist.PlaylistItem
import myLocal.db.LocalDb

fun LocalDb.savePlaylist(
    playlistName: String, playlistDescription: String
) {
    addPlaylistQueries.transaction {
        addPlaylistQueries.upsertAddPlaylist(playlistName, playlistDescription)
    }
}

fun LocalDb.getAddPlaylist() : List<AddPlaylist> {
   return addPlaylistQueries.getFavoritePlaylist().executeAsList()
}