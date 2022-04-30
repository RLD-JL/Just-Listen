package com.rld.justlisten.viewmodel.screens.library

import com.rld.justlisten.datalayer.datacalls.library.saveSongToFavorites
import com.rld.justlisten.datalayer.datacalls.library.saveSongToRecent
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.Events
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState

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
