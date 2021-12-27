package com.example.audius.datalayer.datacalls.search

import com.example.audius.datalayer.Repository
import com.example.audius.datalayer.webservices.apis.searchcalls.searchFor

fun Repository.saveSearch(search : String) {
    localDb.saveSearchInfo(search)
}

fun Repository.getSearchList() : List<String> {
    return localDb.getSearchInfo()
}

suspend fun Repository.searchFor(search: String) : List<String> {
    return webservices.searchFor(search)?.data?.map { playlistModel ->
        playlistModel.id
    }?.toList() ?: emptyList()
}