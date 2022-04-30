package com.example.justlisten.datalayer.localdb.libraryscreen

import com.example.justlisten.datalayer.models.PlayListModel
import com.example.justlisten.datalayer.models.SongIconList
import com.example.justlisten.datalayer.models.UserModel
import myLocal.db.LocalDb

fun LocalDb.saveSongToFavorites(
    id: String, title: String, user: UserModel, songImgList: SongIconList,
    playlistName: String,
    isFavorite: Boolean
) {
    libraryQueries.transaction {
        libraryQueries.upsertLibraryFavorite(
            id,
            title,
            user,
            songImgList,
            playlistName,
            favoriteSong = isFavorite
        )
    }
}

fun LocalDb.getFavoritePlaylist(): List<PlayListModel> {
    return libraryQueries.getFavoritePlaylist(mapper = { id, title, user, songImgList, _, _, _ ->
        PlayListModel(
            id = id,
            playlistTitle = title,
            title = title,
            user = user,
            songImgList = songImgList
        )
    }).executeAsList()
}

fun LocalDb.getCustomPlaylistSongs(songsList: List<String>): List<PlayListModel> {
    return libraryQueries.getCustomPlaylistSongs(
        songsList, mapper = {
            id, title, user, songImgList, playlistName, _, _
            ->
            PlayListModel(id, title, title, songImgList, user)
        }).executeAsList()
}

fun LocalDb.getFavoritePlaylistWithId(id: String): String? {
    return libraryQueries.getFavoritePlaylistWithId(id).executeAsOneOrNull()
}

fun LocalDb.saveSongRecentSongs(
    id: String, title: String, user: UserModel, songImgList: SongIconList,
    playlistName: String
) {
    libraryQueries.transaction {
        libraryQueries.upsertLibraryRecent(id, title, user, songImgList, playlistName)
    }
}

fun LocalDb.getRecentPlayed(): List<PlayListModel> {
    return libraryQueries.getRecentPlayed(mapper = { id, title, user, songImgList, _, _, _ ->
        PlayListModel(
            id = id,
            playlistTitle = title,
            title = title,
            user = user,
            songImgList = songImgList
        )
    }).executeAsList()
}