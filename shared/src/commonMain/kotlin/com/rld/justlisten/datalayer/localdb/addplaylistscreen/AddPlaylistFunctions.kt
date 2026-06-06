package com.rld.justlisten.datalayer.localdb.addplaylistscreen

import com.rld.justlisten.LocalDb
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

fun LocalDb.savePlaylist(
    playlistName: String, playlistDescription: String?, isRemote: Boolean = false, isPrivate: Boolean = false
) {
    addPlaylistQueries.transaction {
        addPlaylistQueries.upsertAddPlaylist(playlistName, playlistDescription, isRemote, isPrivate)
    }
}

fun LocalDb.updatePlaylistSongs(
    playlistName: String, playlistDescription: String?, songList: List<String>, isRemote: Boolean = false, isPrivate: Boolean = false
) {
    addPlaylistQueries.transaction {
        addPlaylistQueries.upsertAddPlaylistWithSongs(
            playlistName,
            playlistDescription, songList, isRemote, isPrivate
        )
    }
}

fun LocalDb.getAddPlaylist(): List<AddPlaylist> {
    return addPlaylistQueries.getAddPlaylist().executeAsList()
}

fun LocalDb.getAddPlaylistFlow(): Flow<List<AddPlaylist>> {
    return addPlaylistQueries.getAddPlaylist().asFlow().mapToList(Dispatchers.IO)
}

fun LocalDb.deletePlaylist(playlistName: String) {
    addPlaylistQueries.deletePlaylist(playlistName)
}

fun LocalDb.updatePlaylistName(oldName: String, newName: String) {
    addPlaylistQueries.updatePlaylistName(newPlaylistName = newName, oldPlaylistName = oldName)
}


