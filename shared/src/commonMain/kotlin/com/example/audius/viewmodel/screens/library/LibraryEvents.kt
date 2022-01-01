package com.example.audius.viewmodel.screens.library

import com.example.audius.datalayer.datacalls.library.saveSongToFavorites
import com.example.audius.datalayer.datacalls.library.saveSongToRecent
import com.example.audius.datalayer.models.SongIconList
import com.example.audius.datalayer.models.UserModel
import com.example.audius.viewmodel.Events

fun Events.saveSongToFavorites(
    id: String,
    title: String,
    user: UserModel,
    songImgList: SongIconList,
    playListName: String = "Favorite"
) = screenCoroutine {
    dataRepository.saveSongToFavorites(id, title, user, songImgList, playListName)
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
