package com.example.audius.datalayer.datacalls

import com.example.audius.datalayer.Repository
import com.example.audius.datalayer.localdb.getTrendingList
import com.example.audius.datalayer.localdb.setTrendingList
import com.example.audius.datalayer.webservices.apis.trendingcalls.TrendingListResponse
import com.example.audius.datalayer.webservices.apis.trendingcalls.fetchTrackListFromPlaylist
import com.example.audius.datalayer.webservices.apis.trendingcalls.fetchTrendingList
import com.example.audius.viewmodel.screens.trending.TrendingListItem

suspend fun Repository.getTrendingListData(): List<TrendingListItem> = withRepoContext {

    webservices.fetchTrendingList()?.apply {
        if(error==null) {
            localDb.setTrendingList(data.sortedByDescending { it.favouriteCount })
        } else {
            //Nothing
        }
    }

    localDb.getTrendingList().map {
            elem->TrendingListItem(_data = elem)
    }.toList()
}

suspend fun Repository.getTrendingList(): List<TrendingListItem> = webservices.fetchTrendingList()?.data?.map {
        trendingListModel -> TrendingListItem(_data = trendingListModel)
} ?: emptyList()


suspend fun Repository.getTrackListFromPlaylist(playlistId: String): List<TrendingListItem> = webservices.fetchTrackListFromPlaylist(playlistId)?.data?.map {
        trendingListModel -> TrendingListItem(_data = trendingListModel)
} ?: emptyList()



