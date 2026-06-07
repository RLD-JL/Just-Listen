package com.rld.justlisten.datalayer.localdb.libraryscreen

import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.LocalDb

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow

fun LocalDb.saveSongToFavorites(
    id: String, title: String, user: UserModel, songImgList: SongIconList,
    playlistName: String,
    isFavorite: Boolean
) {
    libraryQueries.transaction {
        libraryQueries.upsertLibraryFavorite(
            id = id,
            title = title,
            user = user,
            songImgList = songImgList,
            playlistName = playlistName,
            favoriteSong = isFavorite,
            artistId = user.id
        )
    }
}

fun LocalDb.getFavoritePlaylist(): List<PlayListModel> {
    return libraryQueries.getFavoritePlaylist(mapper = { id, title, user, songImgList, _, _, _, _, _, _ ->
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

fun LocalDb.getFavoritePlaylistFlow(): Flow<List<PlayListModel>> {
    return libraryQueries.getFavoritePlaylist(mapper = { id, title, user, songImgList, _, _, _, _, _, _ ->
        PlayListModel(
            id = id,
            playlistTitle = title,
            title = title,
            user = user,
            songImgList = songImgList,
            isStreamable = true
        )
    }).asFlow().mapToList(Dispatchers.IO)
}

fun LocalDb.getCustomPlaylistSongs(songsList: List<String>): List<PlayListModel> {
    return libraryQueries.getCustomPlaylistSongs(
        songsList, mapper = { id, title, user, songImgList, _, _, _, _, _, _
            ->
            PlayListModel(id, title, title, songImgList, user, isStreamable = true)
        }).executeAsList()
}

fun LocalDb.getSongWithId(songId: String): PlayListModel? {
    return libraryQueries.getSongWithId(songId, mapper = { id, title, user, songImgList, _, _, _, _, _, _
        -> PlayListModel(id, title, title, songImgList, user)
    }).executeAsOneOrNull()
}

fun LocalDb.getFavoritePlaylistWithId(id: String): String? {
    return libraryQueries.getFavoritePlaylistWithId(id).executeAsOneOrNull()
}

fun LocalDb.saveSongRecentSongs(
    id: String, title: String, user: UserModel, songImgList: SongIconList,
    playlistName: String
) {
    libraryQueries.transaction {
        libraryQueries.upsertLibraryRecent(
            id = id,
            title = title,
            user = user,
            songImgList = songImgList,
            playlistName = playlistName,
            artistId = user.id
        )
    }
}

fun LocalDb.saveMostPlayedSongs(
    id: String, title: String, user: UserModel, songImgList: SongIconList,
    playlistName: String
) {
    libraryQueries.transaction {
        libraryQueries.upsertLibraryMostPlayed(
            id = id,
            title = title,
            user = user,
            songImgList = songImgList,
            playlistName = playlistName,
            artistId = user.id
        )
    }
}

fun LocalDb.getRecentPlayed(numberOfLines: Long): List<PlayListModel> {
    return libraryQueries.getRecentPlayed(
        mapper = { id, title, user, songImgList, _, _, isFavorite, _, _, _ ->
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
        mapper = { id, title, user, songImgList, _, _, _, _, songCounter, _ ->
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

fun LocalDb.getTimeCapsuleSongs(limit: Long): List<PlayListModel> {
    return libraryQueries.getTimeCapsuleSongs(limit = limit,
        mapper = { id, title, user, songImgList, _, _, _, _, songCounter, _ ->
            PlayListModel(
                id = id,
                playlistTitle = title,
                title = title,
                user = user,
                songImgList = songImgList,
                songCounter = songCounter?.toString() ?: "0",
                isStreamable = true
            )
        }).executeAsList()
}

fun LocalDb.insertPlayLog(songId: String, timestamp: Long, durationPlayedSec: Long, completed: Boolean) {
    libraryQueries.transaction {
        libraryQueries.insertPlayLog(
            songId = songId,
            timestamp = timestamp,
            durationPlayedSec = durationPlayedSec,
            completed = if (completed) 1L else 0L
        )
    }
}

fun LocalDb.getTotalPlaysFromHistory(): Long {
    return libraryQueries.getTotalPlays().executeAsOne()
}

fun LocalDb.getUniquePlayedCountFromHistory(): Long {
    return libraryQueries.getUniquePlayedCount().executeAsOne()
}

fun LocalDb.getTotalDurationPlayedFromHistory(): Long {
    return libraryQueries.getTotalDurationPlayed().executeAsOne()
}

fun LocalDb.getDurationPlayedForSongFromHistory(songId: String): Long {
    return libraryQueries.getDurationPlayedForSong(songId).executeAsOne()
}

fun LocalDb.getDurationPlayedForArtistFromHistory(user: UserModel): Long {
    return libraryQueries.getDurationPlayedForArtist(artistId = user.id).executeAsOne()
}

fun LocalDb.getMostPlayedSongsFromHistory(limit: Long, offset: Long): List<PlayListModel> {
    return libraryQueries.getMostPlayedFromHistory(
        limit = limit,
        offset = offset,
        mapper = { id, title, user, songImgList, _, songCounter, durationPlayedSec ->
            PlayListModel(
                id = id,
                playlistTitle = title,
                title = title,
                user = user,
                songImgList = songImgList,
                songCounter = songCounter.toString(),
                durationPlayedSec = durationPlayedSec,
                isStreamable = true
            )
        }
    ).executeAsList()
}

fun LocalDb.getTopArtistFromHistory(): Triple<UserModel, Long, Long>? {
    return libraryQueries.getTopArtistFromHistory(mapper = { user, plays, totalDurationSec ->
        Triple(user, plays, totalDurationSec)
    }).executeAsOneOrNull()
}