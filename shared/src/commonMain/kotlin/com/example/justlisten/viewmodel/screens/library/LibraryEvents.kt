package com.example.justlisten.viewmodel.screens.library

import com.example.justlisten.datalayer.datacalls.library.saveSongToFavorites
import com.example.justlisten.datalayer.datacalls.library.saveSongToRecent
import com.example.justlisten.datalayer.models.SongIconList
import com.example.justlisten.datalayer.models.UserModel
import com.example.justlisten.viewmodel.Events
import com.example.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState

fun Events.saveSongToFavorites(
    id: String,
    title: String,
    user: UserModel,
    songImgList: SongIconList,
    playListName: String = "Favorite",
    isFavorite: Boolean
) = screenCoroutine {
    dataRepository.saveSongToFavorites(id, title, user, songImgList, playListName, isFavorite)
}

fun Events.saveSongToRecent(
    id: String,
    title: String,
    user: UserModel,
    songImgList: SongIconList,
    playListName: String = "Recent"
) = screenCoroutine {
    dataRepository.saveSongToRecent(id, title, user, songImgList, playListName)
}
