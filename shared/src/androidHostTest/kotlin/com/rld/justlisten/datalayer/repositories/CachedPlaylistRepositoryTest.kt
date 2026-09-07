package com.rld.justlisten.datalayer.repositories

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.localdb.libraryscreen.saveSongToFavorites
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.util.SecureStorage
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class CachedPlaylistRepositoryTest {
    @Test
    fun favoritesReturnWithoutNetworkAndIdsExcludeUnfavorites() = runBlocking {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        LocalDb.Schema.create(driver)
        val db = LocalDb(driver, Repository.addPlaylistAdapter, Repository.libraryAdapter,
            Repository.playlistDetailAdapter, Repository.syncQueueAdapter)
        var requests = 0
        val storage = object : SecureStorage {
            override fun saveToken(key: String, value: String) = Unit
            override fun getToken(key: String): String? = null
            override fun clear() = Unit
        }
        val api = ApiClient(secureStorage = storage, httpClientEngine = MockEngine {
            requests++
            respond("{}")
        })
        try {
            db.saveSongToFavorites("a", "Cached song", UserModel(), SongIconList(), "Favorite", true)
            db.saveSongToFavorites("b", "Not favorite", UserModel(), SongIconList(), "Favorite", false)
            val rows = PlaylistRepositoryImpl(db, api).getPlaylist(0, PlayListEnum.FAVORITE)
            assertEquals(listOf("a"), rows.map { it.id })
            assertEquals("Cached song", rows.single().title)
            assertTrue(rows.single().isFavorite)
            assertEquals(listOf("a"), db.libraryQueries.getFavoriteIds().executeAsList())
            assertEquals(0, requests)
        } finally {
            api.client.close()
            driver.close()
        }
    }
}
