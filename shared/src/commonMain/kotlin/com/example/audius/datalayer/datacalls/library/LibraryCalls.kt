package com.example.audius.datalayer.datacalls.library

import com.example.audius.datalayer.Repository
import com.example.audius.datalayer.localdb.libraryscreen.getFavoritePlaylist
import com.example.audius.datalayer.localdb.libraryscreen.getRecentPlayed
import com.example.audius.datalayer.localdb.libraryscreen.saveSongRecentSongs
import com.example.audius.datalayer.localdb.libraryscreen.saveSongToFavorites
import com.example.audius.datalayer.models.PlayListModel
import com.example.audius.datalayer.models.SongIconList
import com.example.audius.datalayer.models.UserModel

fun Repository.saveSongToFavorites(id: String, title : String, user: UserModel, songImgList: SongIconList,
                                   playlistName: String) {
    localDb.saveSongToFavorites(id, title, user, songImgList, playlistName)
}

fun Repository.saveSongToRecent(id: String, title : String, user: UserModel, songImgList: SongIconList,
                                   playlistName: String) {
    localDb.saveSongRecentSongs(id, title, user, songImgList, playlistName)
}

fun Repository.getFavoritePlaylist(): List<PlayListModel> {
    return localDb.getFavoritePlaylist()
}

fun Repository.getRecentSongs(): List<PlayListModel> {
    return localDb.getRecentPlayed()
}