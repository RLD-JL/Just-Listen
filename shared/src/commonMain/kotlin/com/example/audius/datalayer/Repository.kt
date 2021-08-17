package com.example.audius.datalayer

import com.example.audius.datalayer.webservices.ApiClient
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myLocal.db.LocalDb


class Repository(val sqlDriver: SqlDriver, val useDefaultDispatcher: Boolean = true) {

    internal val webservices by lazy { ApiClient() }
    internal val localDb by lazy { LocalDb(sqlDriver) }

    // we run each repository function on a Dispatchers.Default coroutine
    // we pass useDefaultDispatcher=false just for the TestRepository instance
    suspend fun <T> withRepoContext (block: suspend () -> T) : T {
        return if (useDefaultDispatcher) {
            withContext(Dispatchers.Default) {
                block()
            }
        } else {
            block()
        }
    }

}