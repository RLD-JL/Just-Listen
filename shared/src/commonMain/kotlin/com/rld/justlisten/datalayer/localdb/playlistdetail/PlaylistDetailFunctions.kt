package com.rld.justlisten.datalayer.localdb.playlistdetail

import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.models.PlayListModel

fun LocalDb.getPlaylistDetail() : List<PlayListModel> {
  return  playlistDetailQueries.getPlaylistDetail(mapper = ::PlayListModel).executeAsList()
}

fun LocalDb.setPlaylistDetail(list: List<PlayListModel>) {
    playlistDetailQueries.transaction {
        list.forEach {
            playlistDetailQueries.upsertPlaylistDetail(
                title = it.title,
                id = it.id,
                user = it.user,
                playlistName = it.playlistTitle,
                songImgList = it.songImgList,
            )
        }
    }
}

