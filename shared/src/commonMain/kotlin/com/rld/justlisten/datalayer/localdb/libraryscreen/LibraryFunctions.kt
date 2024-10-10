package com.rld.justlisten.datalayer.localdb.libraryscreen

import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.LocalDb

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
    return libraryQueries.getFavoritePlaylist(mapper = { id, title, user, songImgList, _, _, _, _, _ ->
        PlayListModel(
            id = id,
            playlistTitle = title,
            title = title,
            user = user,
            songImgList = songImgList,
            isStreamable = true
        )
    }).executeAsList()
}

fun LocalDb.getCustomPlaylistSongs(songsList: List<String>): List<PlayListModel> {
    return libraryQueries.getCustomPlaylistSongs(
        songsList, mapper = { id, title, user, songImgList, _, _, _, _, _
            ->
            PlayListModel(id, title, title, songImgList, user, isStreamable = true)
        }).executeAsList()
}

fun LocalDb.getSongWithId(songId: String): PlayListModel {
    return libraryQueries.getSongWithId(songId, mapper = { id, title, user, songImgList, _, _, _, _, _
        -> PlayListModel(id, title, title, songImgList, user)
    }).executeAsOne()
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

fun LocalDb.saveMostPlayedSongs(
    id: String, title: String, user: UserModel, songImgList: SongIconList,
    playlistName: String
) {
    libraryQueries.transaction {
        libraryQueries.upsertLibraryMostPlayed(id, title, user, songImgList, playlistName)
    }
}

fun LocalDb.getRecentPlayed(numberOfLines: Long): List<PlayListModel> {
    return libraryQueries.getRecentPlayed(
        mapper = { id, title, user, songImgList, _, _, isFavorite, _, _ ->
            PlayListModel(
                id = id,
                playlistTitle = title,
                title = title,
                user = user,
                songImgList = songImgList,
                isFavorite = isFavorite ?: false,
                isStreamable = true
            )
        },
        numberOfSongs = numberOfLines
    ).executeAsList()
}

fun LocalDb.getMostPlayedSongs(numberOfSongs: Long): List<PlayListModel> {
    return libraryQueries.getMostPlayed(numberOfSongs = numberOfSongs,
        mapper = { id, title, user, songImgList, _, _, _, _, songCounter ->
            PlayListModel(
                id = id,
                playlistTitle = title,
                title = title,
                user = user,
                songImgList = songImgList,
                songCounter = songCounter.toString(),
                isStreamable = true
            )
        }).executeAsList()
}