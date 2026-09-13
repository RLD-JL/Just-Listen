package com.rld.justlisten.datalayer.repositories

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylist
import com.rld.justlisten.datalayer.localdb.libraryscreen.saveSongToFavorites
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.SyncSession
import com.rld.justlisten.datalayer.webservices.SyncSessionChanged
import com.rld.justlisten.util.SecureStorage
import com.rld.justlisten.util.authSessionLock
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import com.rld.justlisten.datalayer.webservices.apis.authcalls.MeResponse
import java.lang.reflect.Proxy
import kotlin.test.*

class SyncRepositoryIntegrationTest {
    // Ktor uses real dispatchers internally; virtual time can expire HTTP timeouts
    // before its mock engine runs. Barriers below still control each race exactly.
    private fun integrationTest(block: suspend CoroutineScope.() -> Unit) = runBlocking {
        withTimeout(10_000, block)
    }
    private class Storage : SecureStorage {
        private val values = mutableMapOf<String, String>()
        private var generation = 0
        override fun saveToken(key: String, value: String) { values[key] = value }
        override fun getToken(key: String) = values[key]
        override fun clear() { values.clear() }
        fun login(user: String) = authSessionLock.withLock {
            saveToken("auth_session_id", user + "-session-" + generation++)
            saveToken("user_id", user)
            saveToken("access_token", user + "-token")
            saveToken("refresh_token", user + "-refresh")
        }
    }

    private class Fixture(
        scope: CoroutineScope,
        private val dispatcher: CoroutineDispatcher,
        handler: MockRequestHandler,
    ) : AutoCloseable {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        val db: LocalDb
        val storage = Storage().apply { login("A") }
        val api = ApiClient(secureStorage = storage, httpClientEngine = MockEngine(
            MockEngineConfig().apply {
                this.dispatcher = dispatcher
                addHandler(handler)
            }
        ))
        val sync: SyncRepositoryImpl

        init {
            LocalDb.Schema.create(driver)
            db = LocalDb(driver, Repository.addPlaylistAdapter, Repository.libraryAdapter,
                Repository.playlistDetailAdapter, Repository.syncQueueAdapter)
            sync = SyncRepositoryImpl(db, api, storage, NoOpSyncRetryScheduler(),
                scope, dispatcher, startAutomatically = false)
        }

        private val auth by lazy {
            Proxy.newProxyInstance(AuthRepository::class.java.classLoader,
                arrayOf(AuthRepository::class.java)) { _, method, _ ->
                check(method.name == "getSessionState")
                MutableStateFlow<SessionState>(SessionState.Authenticated(
                    MeResponse(userId = "A", name = "A", handle = "a", verified = false, profilePicture = null)))
            } as AuthRepository
        }

        suspend fun favorite(id: String, value: Boolean) {
            FavoritesRepositoryImpl(db, auth, sync, dispatcher).saveSongToFavorites(
                id, id, UserModel(), SongIconList(), "Favorite", value)
        }

        override fun close() { api.client.close(); driver.close() }
    }

    private fun MockRequestHandleScope.json(body: String, status: HttpStatusCode = HttpStatusCode.OK) =
        respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))

    @Test
    fun newerUnfavoriteDuringDownloadSurvivesReconciliation() = integrationTest {
        val fetching = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        Fixture(this, Dispatchers.Default) { request ->
            when (request.url.encodedPath) {
                "/v1/users/A/favorites" -> {
                    fetching.complete(Unit)
                    release.await()
                    json("""{"data":[{"favorite_item_id":"remote","favorite_type":"track","user_id":"A","created_at":""}]}""")
                }
                "/v1/tracks/remote" -> json("""{"data":{"id":"track","title":"Track"}}""")
                else -> json("""{"data":[]}""")
            }
        }.use { f ->
            f.favorite("track", true)
            val inbound = launch { f.sync.performInboundSync("A") }
            fetching.await()
            f.favorite("track", false)
            release.complete(Unit)
            inbound.join()
            assertTrue(f.db.getFavoritePlaylist().isEmpty())
            assertEquals(listOf("UNFAVORITE"), f.db.syncQueueQueries.getPendingTasks("A")
                .executeAsList().map { it.actionType })
        }
    }

    @Test
    fun switchedAccountCannotReceiveRemainingOldAccountWrites() = integrationTest {
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val requests = mutableListOf<Pair<String?, String?>>()
        Fixture(this, Dispatchers.Default) { request ->
            requests += request.headers[HttpHeaders.Authorization] to request.url.parameters["user_id"]
            started.complete(Unit)
            release.await()
            json("{}")
        }.use { f ->
            f.favorite("one", true)
            f.favorite("two", true)
            val drain = async { f.sync.runPendingSync() }
            started.await()
            f.storage.login("B")
            release.complete(Unit)
            drain.await()
            assertEquals<List<Pair<String?, String?>>>(listOf("Bearer A-token" to "A"), requests)
            assertEquals(2, f.db.syncQueueQueries.getPendingTasks("A").executeAsList().size)
        }
    }

    @Test
    fun oldInboundResponseCannotReplaceNewAccountsLibrary() = integrationTest {
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        Fixture(this, Dispatchers.Default) {
            started.complete(Unit)
            release.await()
            json("""{"data":[]}""")
        }.use { f ->
            f.db.saveSongToFavorites("local", "Local", UserModel(), SongIconList(), "Favorite", true)
            f.db.favoriteSyncQueries.markFavoriteSyncInitialized("A")
            f.db.favoriteSyncQueries.insertFavoriteSyncBaseline("A", "local")
            val inbound = launch { f.sync.performInboundSync("A") }
            started.await()
            f.storage.login("B")
            release.complete(Unit)
            inbound.join()
            assertEquals(listOf("local"), f.db.getFavoritePlaylist().map { it.id })
        }
    }

    @Test
    fun unavailableTrackPreservesLocalFavoritesAndDoesNotBlockPlaylists() = integrationTest {
        var playlistRequested = false
        Fixture(this, Dispatchers.Default) { request ->
            when (request.url.encodedPath) {
                "/v1/users/A/favorites" -> json("""{"data":[{"favorite_item_id":"missing","favorite_type":"track","user_id":"A","created_at":""},{"favorite_item_id":"remote","favorite_type":"track","user_id":"A","created_at":""}]}""")
                "/v1/tracks/missing" -> json("{}", HttpStatusCode.NotFound)
                "/v1/tracks/remote" -> json("""{"data":{"id":"new","title":"New"}}""")
                "/v1/users/A/playlists" -> {
                    playlistRequested = true
                    json("""{"data":[]}""")
                }
                else -> error("Unexpected request")
            }
        }.use { f ->
            f.db.saveSongToFavorites("local", "Local", UserModel(), SongIconList(), "Favorite", true)
            f.db.favoriteSyncQueries.markFavoriteSyncInitialized("A")
            f.db.favoriteSyncQueries.insertFavoriteSyncBaseline("A", "local")
            f.sync.performInboundSync("A")
            assertEquals(setOf("local", "new"), f.db.getFavoritePlaylist().map { it.id }.toSet())
            assertEquals(listOf("local"), f.db.favoriteSyncQueries.getFavoriteSyncBaseline("A").executeAsList())
            assertTrue(playlistRequested)
            assertTrue(f.db.syncQueueQueries.getPendingTasks("A").executeAsList().isEmpty())
        }
    }

    @Test
    fun tokenRefreshCannotOverwriteCredentialsFromANewerLogin() = integrationTest {
        val refreshing = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        Fixture(this, Dispatchers.Default) { request ->
            if (request.url.encodedPath.endsWith("/oauth/token")) {
                refreshing.complete(Unit)
                release.await()
                json("""{"access_token":"old-refresh-result","refresh_token":"old-refresh-token"}""")
            } else json("{}", HttpStatusCode.Unauthorized)
        }.use { f ->
            val session = SyncSession.capture(f.storage, "A")
            val job = async {
                try {
                    withContext(session) { f.api.getResponse<String>("/test") }
                    false
                } catch (_: SyncSessionChanged) { true }
            }
            refreshing.await()
            f.storage.login("B")
            release.complete(Unit)
            assertTrue(job.await())
            assertEquals("B-token", f.storage.getToken("access_token"))
            assertEquals("B-refresh", f.storage.getToken("refresh_token"))
        }
    }

    @Test
    fun failedEnqueueRollsBackLocalFavoriteEdit() = integrationTest {
        Fixture(this, Dispatchers.Default) { json("{}") }.use { f ->
            f.driver.execute(null, """CREATE TRIGGER reject_sync BEFORE INSERT ON SyncQueue
                BEGIN SELECT RAISE(ABORT, 'test queue failure'); END""", 0)
            assertFailsWith<Exception> { f.favorite("track", true) }
            assertTrue(f.db.getFavoritePlaylist().isEmpty())
        }
    }

    @Test
    fun newerIntentSurvivesCompletionOfAnOlderInFlightWrite() = integrationTest {
        val started = CompletableDeferred<Unit>()
        val release = CompletableDeferred<Unit>()
        val methods = mutableListOf<HttpMethod>()
        Fixture(this, Dispatchers.Default) { request ->
            methods += request.method
            if (methods.size == 1) {
                started.complete(Unit)
                release.await()
            }
            json("{}")
        }.use { f ->
            f.favorite("track", true)
            val drain = async { f.sync.runPendingSync() }
            started.await()
            f.favorite("track", false)
            release.complete(Unit)
            assertTrue(drain.await())
            assertEquals(listOf(HttpMethod.Post, HttpMethod.Delete), methods)
            assertTrue(f.db.getFavoritePlaylist().isEmpty())
            assertTrue(f.db.syncQueueQueries.getPendingTasks("A").executeAsList().isEmpty())
        }
    }

    @Test
    fun scopedWritesCanRefreshTheirOwnToken() = integrationTest {
        val bearerHeaders = mutableListOf<List<String>>()
        Fixture(this, Dispatchers.Default) { request ->
            if (request.url.encodedPath.endsWith("/oauth/token")) {
                json("""{"access_token":"renewed","refresh_token":"renewed-refresh"}""")
            } else {
                bearerHeaders += request.headers.getAll(HttpHeaders.Authorization).orEmpty()
                if (bearerHeaders.size == 1) json("{}", HttpStatusCode.Unauthorized) else json("{}")
            }
        }.use { f ->
            f.favorite("track", true)
            assertTrue(f.sync.runPendingSync())
            assertEquals(listOf(listOf("Bearer A-token"), listOf("Bearer renewed")), bearerHeaders)
        }
    }

    @Test
    fun reloginToSameAccountInvalidatesOldSession() = integrationTest {
        var sent = false
        Fixture(this, Dispatchers.Default) {
            sent = true
            json("{}")
        }.use { f ->
            val oldSession = SyncSession.capture(f.storage, "A")
            f.storage.login("A")
            assertFailsWith<SyncSessionChanged> {
                withContext(oldSession) { f.api.getResponse<String>("/test") }
            }
            assertFalse(sent)
        }
    }

    @Test
    fun migrationPreservesButDoesNotDispatchLegacyOperations() {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            driver.execute(null, """CREATE TABLE SyncQueue (id INTEGER PRIMARY KEY AUTOINCREMENT,
                actionType TEXT NOT NULL, targetId TEXT NOT NULL, payload TEXT,
                errorMessage TEXT, retryCount INTEGER NOT NULL DEFAULT 0)""", 0)
            driver.execute(null, """INSERT INTO SyncQueue(actionType,targetId,payload)
                VALUES ('UNFAVORITE','track',NULL),('PLAYLIST_UPDATE','playlist','original-payload')""", 0)
            LocalDb.Schema.migrate(driver, 14, LocalDb.Schema.version)
            val db = LocalDb(driver, Repository.addPlaylistAdapter, Repository.libraryAdapter,
                Repository.playlistDetailAdapter, Repository.syncQueueAdapter)
            assertTrue(db.syncQueueQueries.getPendingTasks("A").executeAsList().isEmpty())
            val quarantined = db.syncQueueQueries.getPendingTasks("").executeAsList()
            assertEquals(2, quarantined.size)
            assertEquals("original-payload", quarantined.last().payload)
            assertTrue(quarantined.all { it.errorMessage?.contains("ownership") == true })
        }
    }
}
