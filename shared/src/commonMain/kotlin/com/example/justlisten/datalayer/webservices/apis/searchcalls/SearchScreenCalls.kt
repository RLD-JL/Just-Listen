package com.example.justlisten.datalayer.webservices.apis.searchcalls

import com.example.justlisten.datalayer.webservices.ApiClient
import com.example.justlisten.datalayer.webservices.apis.playlistcalls.PlayListResponse
import com.example.justlisten.viewmodel.screens.search.SearchEnum
import com.example.justlisten.viewmodel.screens.search.SearchEnum.*

suspend fun ApiClient.searchFor(searchFor: String, searchEnum: SearchEnum): PlayListResponse? {
    return when(searchEnum) {
        TRACKS -> getResponse("/tracks/search?query=${searchFor}&app_name=EXAMPLEAPP")
        PLAYLIST -> getResponse("/playlists/search?query=${searchFor}&app_name=EXAMPLEAPP")
    }
}