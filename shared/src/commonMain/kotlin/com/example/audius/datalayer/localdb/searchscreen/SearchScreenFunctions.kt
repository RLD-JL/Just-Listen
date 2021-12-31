package com.example.audius.datalayer.localdb.searchscreen

import myLocal.db.LocalDb

fun LocalDb.saveSearchInfo(searchFor: String) {
    searchScreenInfoQueries.transaction {
        searchScreenInfoQueries.upsertSearchScreenInfo(searchFor)
    }
}

fun LocalDb.getSearchInfo(): List<String> {
    return searchScreenInfoQueries.getSearchScreenInfo().executeAsList()
}