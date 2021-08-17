package com.example.audius.datalayer.localdb

import com.example.audius.datalayer.models.TrendingListModel
import myLocal.db.LocalDb

fun LocalDb.getTrendingList() : List<TrendingListModel> {
   return trendingQueries.getTrendingList(mapper = ::TrendingListModel).executeAsList()
}

fun LocalDb.setTrendingList(list: List<TrendingListModel>) {
   trendingQueries.transaction {
      list.forEach {
         trendingQueries.upsertTrending(
            title = it.title,
            id = it.id,
            favoriteCount = it.favouriteCount,
            repostCount = it.repostCount
         )
      }
   }
}
