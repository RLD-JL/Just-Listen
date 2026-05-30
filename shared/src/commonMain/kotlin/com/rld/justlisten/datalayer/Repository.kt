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
import com.rld.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylistFlow
import com.rld.justlisten.datalayer.models.PlayListModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString


class Repository(
    private val _localDb: LocalDb?,
    internal val webservices: ApiClient,
    private val useDefaultDispatcher: Boolean = true
) {
    internal val localDb: LocalDb get() = _localDb ?: error("LocalDb not initialized")

    companion object {
        val listOfStringsAdapter = object : ColumnAdapter<SongIconList, String> {
            override fun decode(databaseValue: String): SongIconList {
                return try {
                    Json.decodeFromString(databaseValue)
                } catch (e: Exception) {
                    SongIconList()
                }
            }

            override fun encode(value: SongIconList): String {
                return Json.encodeToString(value)
            }
        }

        val userModelAdapter = object : ColumnAdapter<UserModel, String> {
            override fun decode(databaseValue: String): UserModel {
                return try {
                    Json.decodeFromString(databaseValue)
                } catch (e: Exception) {
                    UserModel()
                }
            }

            override fun encode(value: UserModel): String {
                return Json.encodeToString(value)
            }
        }


        val playlistAdapter = object : ColumnAdapter<List<String>, String> {
            override fun decode(databaseValue: String): List<String> {
                return try {
                    Json.decodeFromString(databaseValue)
                } catch (e: Exception) {
                    emptyList()
                }
            }

            override fun encode(value: List<String>): String {
                return Json.encodeToString(value)
            }
        }

        val playlistDetailAdapter = PlaylistDetail.Adapter(listOfStringsAdapter, userModelAdapter)
        val libraryAdapter = Library.Adapter(userModelAdapter, listOfStringsAdapter)
        val addPlaylistAdapter = AddPlaylist.Adapter(playlistAdapter)
    }

    fun getFavoritePlaylistFlow(): Flow<List<PlayListModel>> {
        return localDb.getFavoritePlaylistFlow()
    }

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