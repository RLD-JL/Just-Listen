package com.rld.justlisten.viewmodel.screens.library

import com.rld.justlisten.datalayer.datacalls.library.getRecentSongs
import com.rld.justlisten.datalayer.datacalls.library.saveSongToFavorites
import com.rld.justlisten.datalayer.datacalls.library.saveSongToMostPlayed
import com.rld.justlisten.datalayer.datacalls.library.saveSongToRecent
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.Events
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem


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

fun Events.saveSongToMostPlayed(
    id: String,
    title: String,
    user: UserModel,
    songImgList: SongIconList,
    playListName: String = "Most Played"
) = screenCoroutine {
    dataRepository.saveSongToMostPlayed(id, title, user, songImgList, playListName)
}

fun Events.getLastPlayed(numberOfSongs: Long) = screenCoroutine {
    stateManager.updateScreen(LibraryState::class) {

        val recentSongs = dataRepository.getRecentSongs(numberOfSongs).map { playlistModel ->
            PlaylistItem(playlistModel, playlistModel.isFavorite)
        }.toList()
        if (recentSongs.size == it.recentSongsItems.size) {
            it.copy(lastIndexReached = true)
        } else {
            it.copy(recentSongsItems = recentSongs)
        }
    }
}