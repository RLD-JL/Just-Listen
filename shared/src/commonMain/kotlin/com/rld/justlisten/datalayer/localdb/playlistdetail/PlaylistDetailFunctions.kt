package com.rld.justlisten.datalayer.localdb.playlistdetail

import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.models.PlayListModel

fun LocalDb.getPlaylistDetail() : List<PlayListModel> {
  return  playlistDetailQueries.getPlaylistDetail(mapper = { id, title, playlistName, songImgList, user ->
      PlayListModel(
          id = id,
          title = title,
          playlistTitle = playlistName,
          songImgList = songImgList,
          user = user
      )
  }).executeAsList()
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

