package com.example.audius.datalayer.datacalls.search

import myLocal.db.LocalDb

fun LocalDb.saveSearchInfo(searchFor: String) {
    searchScreenInfoQueries.transaction {
        searchScreenInfoQueries.upsertSearchScreenInfo(searchFor)
    }
}

fun LocalDb.getSearchInfo() : List<String> {
    return searchScreenInfoQueries.getSearchScreenInfo(mapper = ::SearchScreenInfo).executeAsList().map { element ->
        element.searchFor
    }.toList()
}
