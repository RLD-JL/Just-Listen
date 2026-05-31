package com.rld.justlisten.datalayer.webservices.apis.searchcalls

import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.playlistcalls.PlayListResponse
import com.rld.justlisten.viewmodel.screens.search.SearchEnum
import com.rld.justlisten.viewmodel.screens.search.SearchEnum.*

suspend fun ApiClient.searchFor(searchFor: String, searchEnum: SearchEnum): PlayListResponse? {
    return when (searchEnum) {
        TRACKS -> {
            val response: PlayListResponse? =
                getResponse("/tracks/search?query=$searchFor")
            response?.let {
                PlayListResponse(it.data.filter { track -> track.isStreamable })
            }
        }
        PLAYLIST -> getResponse("/playlists/search?query=$searchFor")
    }
}