package com.rld.justlisten.datalayer

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.webservices.ApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.rld.justlisten.LocalDb
import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.database.libraryscreen.Library
import com.rld.justlisten.database.playlistdetail.PlaylistDetail


class Repository(
    private val sqlDriver: SqlDriver,
    private val useDefaultDispatcher: Boolean = true
) {


    private val listOfStringsAdapter = object : ColumnAdapter<SongIconList, String> {
        override fun decode(databaseValue: String): SongIconList {
            return if (databaseValue.isEmpty())
                SongIconList()
            else {
                val songIconList = databaseValue.split(",")
                SongIconList(songIconList[0], songIconList[1], songIconList[2])
            }
        }

        override fun encode(value: SongIconList): String {
            return value.songImageURL150px + "," + value.songImageURL480px + "," + value.songImageURL1000px
        }
    }

    private val listOfStringsAdapter2 = object : ColumnAdapter<UserModel, String> {
        override fun decode(databaseValue: String): UserModel {
            return if (databaseValue.isEmpty())
                UserModel()
            else {
                UserModel(databaseValue)
            }
        }

        override fun encode(value: UserModel): String {
            return value.username
        }
    }


    private val playlistAdapter = object : ColumnAdapter<List<String>, String> {
        override fun decode(databaseValue: String): List<String> {
            return if (databaseValue.isEmpty())
                emptyList()
            else {
                val mutableList = mutableListOf<String>()

                databaseValue.split(",").forEach {
                    mutableList.add(it)
                }
                return mutableList
            }
        }

        override fun encode(value: List<String>): String {
            return value.joinToString(",")
        }
    }

    private val adapter = PlaylistDetail.Adapter(listOfStringsAdapter, listOfStringsAdapter2)
    private val libraryAdapter = Library.Adapter(listOfStringsAdapter2, listOfStringsAdapter)
    private val addPlaylistAdapter = AddPlaylist.Adapter(playlistAdapter)
    internal val webservices by lazy { ApiClient() }
    internal val localDb by lazy { LocalDb(sqlDriver, addPlaylistAdapter, libraryAdapter, adapter) }

    // we run each repository function on a Dispatchers.Default coroutine
    // we pass useDefaultDispatcher=false just for the TestRepository instance
    suspend fun <T> withRepoContext(block: suspend () -> T): T {
        return if (useDefaultDispatcher) {
            withContext(Dispatchers.Default) {
                block()
            }
        } else {
            block()
        }
    }

}