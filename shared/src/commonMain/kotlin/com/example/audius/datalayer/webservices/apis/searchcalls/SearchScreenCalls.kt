package com.example.audius.datalayer.webservices.apis.searchcalls

import com.example.audius.datalayer.webservices.ApiClient
import com.example.audius.datalayer.webservices.apis.playlistcalls.PlayListResponse

suspend fun ApiClient.searchFor(searchFor: String): PlayListResponse? {
    /*TODO*/
    return getResponse("/tracks/trending?app_name=${searchFor}")
}