package com.rld.justlisten.datalayer.datacalls.library

import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylist
import com.rld.justlisten.datalayer.localdb.libraryscreen.getRecentPlayed
import com.rld.justlisten.datalayer.localdb.libraryscreen.saveSongRecentSongs
import com.rld.justlisten.datalayer.localdb.libraryscreen.saveSongToFavorites
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel

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

fun Repository.getRecentSongs(numberOfLines: Long): List<PlayListModel> {
    return localDb.getRecentPlayed(numberOfLines)
}