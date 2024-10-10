package com.rld.justlisten.datalayer.localdb.searchscreen

import com.rld.justlisten.LocalDb

fun LocalDb.saveSearchInfo(searchFor: String) {
    searchScreenInfoQueries.transaction {
        searchScreenInfoQueries.upsertSearchScreenInfo(searchFor)
    }
}

fun LocalDb.getSearchInfo(): List<String> {
    return searchScreenInfoQueries.getSearchScreenInfo().executeAsList()
}