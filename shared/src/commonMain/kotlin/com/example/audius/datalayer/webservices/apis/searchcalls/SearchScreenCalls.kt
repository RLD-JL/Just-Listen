package com.example.audius.datalayer.webservices.apis.searchcalls

import com.example.audius.datalayer.webservices.ApiClient
import com.example.audius.datalayer.webservices.apis.playlistcalls.PlayListResponse
import com.example.audius.viewmodel.screens.search.SearchEnum
import com.example.audius.viewmodel.screens.search.SearchEnum.*

suspend fun ApiClient.searchFor(searchFor: String, searchEnum: SearchEnum): PlayListResponse? {
    return when(searchEnum) {
        TRACKS -> getResponse("/tracks/search?query=${searchFor}&app_name=EXAMPLEAPP")
        PLAYLIST -> getResponse("/playlists/search?query=${searchFor}&app_name=EXAMPLEAPP")
    }
}