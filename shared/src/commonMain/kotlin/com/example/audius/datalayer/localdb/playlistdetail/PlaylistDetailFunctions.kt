package com.example.audius.datalayer.localdb.playlistdetail

import com.example.audius.datalayer.models.PlayListModel
import com.example.audius.datalayer.models.TrendingListModel
import myLocal.db.LocalDb

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
