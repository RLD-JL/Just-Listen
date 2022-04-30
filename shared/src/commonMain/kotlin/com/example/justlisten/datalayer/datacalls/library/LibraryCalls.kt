package com.example.justlisten.datalayer.datacalls.library

import com.example.justlisten.datalayer.Repository
import com.example.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylist
import com.example.justlisten.datalayer.localdb.libraryscreen.getRecentPlayed
import com.example.justlisten.datalayer.localdb.libraryscreen.saveSongRecentSongs
import com.example.justlisten.datalayer.localdb.libraryscreen.saveSongToFavorites
import com.example.justlisten.datalayer.models.PlayListModel
import com.example.justlisten.datalayer.models.SongIconList
import com.example.justlisten.datalayer.models.UserModel

fun Repository.saveSongToFavorites(id: String, title : String, user: UserModel, songImgList: SongIconList,
                                   playlistName: String, isFavorite: Boolean) {
    localDb.saveSongToFavorites(id, title, user, songImgList, playlistName, isFavorite = isFavorite)
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