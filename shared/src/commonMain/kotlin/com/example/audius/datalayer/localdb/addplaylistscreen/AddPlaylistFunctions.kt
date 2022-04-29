package com.example.audius.datalayer.localdb.addplaylistscreen

import myLocal.db.LocalDb

fun LocalDb.savePlaylist(
    playlistName: String, playlistDescription: String?
) {
    addPlaylistQueries.transaction {
        addPlaylistQueries.upsertAddPlaylist(playlistName, playlistDescription)
    }
}

fun LocalDb.updatePlaylistSongs(
    playlistName: String, playlistDescription: String?, playlistId: Long,songList: String
) {
    addPlaylistQueries.transaction {
        addPlaylistQueries.upserAddPlaylistWithSongs(
            playlistName,
            playlistDescription,
            id = playlistId,
            songsList = songList
        )
    }
}

fun LocalDb.getAddPlaylist(): List<AddPlaylist> {
    return addPlaylistQueries.getAddPlaylist().executeAsList()
}