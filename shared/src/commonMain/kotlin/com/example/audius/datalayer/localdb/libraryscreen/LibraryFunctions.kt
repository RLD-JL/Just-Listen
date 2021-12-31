package com.example.audius.datalayer.localdb.libraryscreen

import com.example.audius.datalayer.models.PlayListModel
import com.example.audius.datalayer.models.SongIconList
import com.example.audius.datalayer.models.UserModel
import myLocal.db.LocalDb

fun LocalDb.saveSongToFavorites(id: String, title : String, user: UserModel, songImgList: SongIconList,
                                playlistName: String) {
    libraryQueries.transaction {
        libraryQueries.upsertLibrary(id, title, user, songImgList, playlistName)
    }
}

fun LocalDb.getFavoritePlaylist(): List<PlayListModel> {
    return  libraryQueries.getFavoritePlaylist(mapper = ::PlayListModel).executeAsList()
    }
