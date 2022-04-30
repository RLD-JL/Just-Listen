package com.rld.justlisten.datalayer.localdb.addplaylistscreen

import myLocal.db.LocalDb

fun LocalDb.savePlaylist(
    playlistName: String, playlistDescription: String?
) {
    addPlaylistQueries.transaction {
        addPlaylistQueries.upsertAddPlaylist(playlistName, playlistDescription)
    }
}

fun LocalDb.updatePlaylistSongs(
    playlistName: String, playlistDescription: String?,songList: List<String>
) {
    addPlaylistQueries.transaction {
        addPlaylistQueries.upserAddPlaylistWithSongs(
            playlistName,
            playlistDescription, songList
        )
    }
}

fun LocalDb.getAddPlaylist(): List<AddPlaylist> {
    return addPlaylistQueries.getAddPlaylist().executeAsList()
}