package com.example.audius.datalayer

import com.example.audius.datalayer.localdb.Trending
import com.example.audius.datalayer.models.SongIconList
import com.example.audius.datalayer.webservices.ApiClient
import com.squareup.sqldelight.ColumnAdapter
import com.squareup.sqldelight.db.SqlDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import myLocal.db.LocalDb


class Repository(private val sqlDriver: SqlDriver, private val useDefaultDispatcher: Boolean = true) {


    private val listOfStringsAdapter = object : ColumnAdapter<SongIconList, String> {
        override fun decode(databaseValue: String): SongIconList {
            return if(databaseValue.isEmpty())
                SongIconList()
            else {
                val songIconList = databaseValue.split(",")
                SongIconList(songIconList[0],songIconList[1],songIconList[2])
            }
        }

        override fun encode(value: SongIconList): String {
            return value.songImageURL150px+","+value.songImageURL480px+","+value.songImageURL1000px
        }
    }

    val adapter = Trending.Adapter(listOfStringsAdapter)
    internal val webservices by lazy { ApiClient() }
    internal val localDb by lazy { LocalDb(sqlDriver, adapter) }

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