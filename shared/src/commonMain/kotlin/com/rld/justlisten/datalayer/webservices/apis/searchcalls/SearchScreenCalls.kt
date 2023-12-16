package com.rld.justlisten.datalayer.webservices.apis.searchcalls

import com.rld.justlisten.datalayer.utils.Constants
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.playlistcalls.PlayListResponse
import com.rld.justlisten.viewmodel.screens.search.SearchEnum
import com.rld.justlisten.viewmodel.screens.search.SearchEnum.*

suspend fun ApiClient.searchFor(searchFor: String, searchEnum: SearchEnum): PlayListResponse? {
    return when (searchEnum) {
        TRACKS -> {
            val response: PlayListResponse? =
                getResponse("/tracks/search?query=${searchFor}&app_name=${Constants.appName}")
            response?.let {
                val tracks = it.data.filter { it.isStreamable }
                PlayListResponse(tracks)
            }
        }
        PLAYLIST -> getResponse("/playlists/search?query=${searchFor}&app_name=${Constants.appName}")
    }
}