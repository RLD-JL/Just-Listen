package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.localdb.libraryscreen.saveSongToFavorites
import com.rld.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylist
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.ApiRequestException
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserFavorites
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getFavoriteTrackDetailsByRemoteId
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserPlaylists
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.updatePlaylistSongs
import com.rld.justlisten.datalayer.localdb.libraryscreen.getCustomPlaylistSongs
import com.rld.justlisten.datalayer.webservices.apis.writecalls.favoriteTrack
import com.rld.justlisten.datalayer.webservices.apis.writecalls.unfavoriteTrack
import com.rld.justlisten.datalayer.webservices.apis.writecalls.createPlaylist
import com.rld.justlisten.datalayer.webservices.apis.writecalls.updatePlaylistSongs
import com.rld.justlisten.datalayer.webservices.apis.writecalls.deletePlaylist
import com.rld.justlisten.datalayer.webservices.apis.writecalls.updatePlaylistDetails
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import co.touchlab.kermit.Logger
import com.rld.justlisten.util.SecureStorage
import com.rld.justlisten.util.authSessionLock
import com.rld.justlisten.datalayer.webservices.SyncSession
import com.rld.justlisten.datalayer.webservices.SyncSessionChanged

sealed interface SyncState {
    object Synced : SyncState
    data class Syncing(val pendingCount: Int) : SyncState
    data class SyncFailed(val errorMessage: String) : SyncState
}

interface SyncRepository {
    val syncState: StateFlow<SyncState>
    fun enqueueFavoriteTask(userId: String, trackId: String, isFavorite: Boolean)
    fun enqueuePlaylistCreateTask(name: String, description: String?, isPrivate: Boolean)
    fun enqueuePlaylistUpdateTask(playlistId: String, songs: List<String>)
    fun enqueuePlaylistDeleteTask(playlistId: String)
    fun enqueuePlaylistDetailsUpdateTask(playlistId: String, name: String, description: String?)
    fun triggerSync()
    fun clearQueue()
    suspend fun runPendingSync(): Boolean
    suspend fun performInboundSync(userId: String)
}

@Serializable
data class PlaylistTaskPayload(
    val name: String,
    val description: String?,
    val isPrivate: Boolean
)

@Serializable
data class PlaylistUpdateTaskPayload(
    val songs: List<String>
)

@Serializable
data class PlaylistDetailsUpdatePayload(
    val name: String,
    val description: String?
)

class SyncRepositoryImpl(
    private val localDb: LocalDb,
    private val apiClient: ApiClient,
    private val secureStorage: SecureStorage,
    private val retryScheduler: SyncRetryScheduler,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val startAutomatically: Boolean = true,
) : SyncRepository {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Synced)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val mutex = kotlinx.coroutines.sync.Mutex()
    private val syncRequests = Channel<Unit>(Channel.CONFLATED)
    private var retryJob: Job? = null

    init {
        val knownActiveUser = localDb.favoriteSyncQueries
            .getActiveFavoriteSyncUser()
            .executeAsOneOrNull()
            ?.activeUserId
        if (knownActiveUser == null) {
            activeUserId()?.let { userId ->
                localDb.favoriteSyncQueries.setActiveFavoriteSyncUser(userId)
            }
        }
        if (startAutomatically) scope.launch {
            for (request in syncRequests) {
                try {
                    drainPendingSync(manageDurableRetry = true)
                } catch (_: SyncSessionChanged) {
                    // Keep the actor alive for a later login; old tasks remain owned.
                }
            }
        }
        if (startAutomatically) triggerSync()
    }

    override fun enqueueFavoriteTask(userId: String, trackId: String, isFavorite: Boolean) {
        if (userId.isBlank()) return
        authSessionLock.withLock {
            if (activeUserId() != userId) return@withLock
            localDb.transaction {
                localDb.syncQueueQueries.deleteFavoriteTasksByTarget(userId, trackId)
                insertFavoriteTask(userId, trackId, isFavorite)
                afterCommit {
                    triggerSync()
                    retryScheduler.scheduleRetry(0L)
                }
            }
        }
    }


    override fun enqueuePlaylistCreateTask(name: String, description: String?, isPrivate: Boolean) {
        val userId = activeUserId() ?: return
        val payloadJson = Json.encodeToString(
            PlaylistTaskPayload.serializer(),
            PlaylistTaskPayload(name, description, isPrivate)
        )
        localDb.syncQueueQueries.insertTask(
            actionType = "PLAYLIST_CREATE",
            targetId = "NEW",
            userId = userId,
            payload = payloadJson,
            errorMessage = null,
            retryCount = 0
        )
        triggerSync()
        retryScheduler.scheduleRetry(0L)
    }

    override fun enqueuePlaylistUpdateTask(playlistId: String, songs: List<String>) {
        val userId = activeUserId() ?: return
        val payloadJson = Json.encodeToString(
            PlaylistUpdateTaskPayload.serializer(),
            PlaylistUpdateTaskPayload(songs)
        )
        localDb.syncQueueQueries.insertTask(
            actionType = "PLAYLIST_UPDATE",
            targetId = playlistId,
            userId = userId,
            payload = payloadJson,
            errorMessage = null,
            retryCount = 0
        )
        triggerSync()
        retryScheduler.scheduleRetry(0L)
    }

    override fun enqueuePlaylistDeleteTask(playlistId: String) {
        val userId = activeUserId() ?: return
        localDb.syncQueueQueries.insertTask(
            actionType = "PLAYLIST_DELETE",
            targetId = playlistId,
            userId = userId,
            payload = null,
            errorMessage = null,
            retryCount = 0
        )
        triggerSync()
        retryScheduler.scheduleRetry(0L)
    }

    override fun enqueuePlaylistDetailsUpdateTask(playlistId: String, name: String, description: String?) {
        val userId = activeUserId() ?: return
        val payloadJson = Json.encodeToString(
            PlaylistDetailsUpdatePayload.serializer(),
            PlaylistDetailsUpdatePayload(name, description)
        )
        localDb.syncQueueQueries.insertTask(
            actionType = "PLAYLIST_DETAILS_UPDATE",
            targetId = playlistId,
            userId = userId,
            payload = payloadJson,
            errorMessage = null,
            retryCount = 0
        )
        triggerSync()
        retryScheduler.scheduleRetry(0L)
    }


    override fun triggerSync() {
        syncRequests.trySend(Unit)
    }

    override fun clearQueue() {
        retryJob?.cancel()
        retryJob = null
        retryScheduler.cancelRetry()
        localDb.syncQueueQueries.deleteAllTasks()
        _syncState.value = SyncState.Synced
    }

    override suspend fun runPendingSync(): Boolean =
        try { drainPendingSync(manageDurableRetry = false) } catch (_: SyncSessionChanged) { true }

    private suspend fun drainPendingSync(manageDurableRetry: Boolean): Boolean = mutex.withLock {
        val userId = activeUserId() ?: return@withLock true
        val session = SyncSession.capture(secureStorage, userId)
        withContext(session) { processQueue(userId, manageDurableRetry) }
    }

    private fun activeUserId(): String? =
        secureStorage.getToken("user_id")?.takeIf { it.isNotBlank() }

    private fun scheduleRetry(retryCount: Int, scheduleDurableRetry: Boolean) {
        retryJob?.cancel()
        val delayMs = (1_000L shl (retryCount - 1).coerceIn(0, 5)).coerceAtMost(30_000L)
        retryJob = scope.launch {
            delay(delayMs)
            triggerSync()
        }
        if (scheduleDurableRetry) {
            retryScheduler.scheduleRetry(delayMs)
        }
    }

    private suspend fun processQueue(userId: String, manageDurableRetry: Boolean): Boolean {
        while (true) {
            val pendingTasks = localDb.syncQueueQueries.getPendingTasks(userId).executeAsList()
            if (pendingTasks.isEmpty()) {
                retryJob?.cancel()
                retryJob = null
                if (manageDurableRetry) {
                    retryScheduler.cancelRetry()
                }
                _syncState.value = SyncState.Synced
                return true
            }

            _syncState.value = SyncState.Syncing(pendingTasks.size)

            val task = pendingTasks.first()

            // Stay within rate limits by introducing a 200ms delay between actions
            delay(200L)
            val session = currentCoroutineContext()[SyncSession]!!
            authSessionLock.withLock { session.requireActive(secureStorage) }

            val success = try {
                when (task.actionType) {
                    "FAVORITE" -> {
                        val response = apiClient.favoriteTrack(task.targetId)
                        val isConflict = response?.error != null && (
                            response.error.contains("already favorited", ignoreCase = true) ||
                            response.error.contains("already exists", ignoreCase = true)
                        )
                        isConflict || (response != null && response.error == null)
                    }
                    "UNFAVORITE" -> {
                        val response = apiClient.unfavoriteTrack(task.targetId)
                        val isConflict = response?.error != null && (
                            response.error.contains("not favorited", ignoreCase = true) ||
                            response.error.contains("does not exist", ignoreCase = true)
                        )
                        isConflict || (response != null && response.error == null)
                    }
                    "PLAYLIST_CREATE" -> {
                        val payload = Json.decodeFromString(
                            PlaylistTaskPayload.serializer(),
                            task.payload ?: ""
                        )
                        val response = apiClient.createPlaylist(
                            name = payload.name,
                            description = payload.description,
                            isPrivate = payload.isPrivate
                        )
                        if (response?.playlistId != null) {
                            authSessionLock.withLock {
                                session.requireActive(secureStorage)
                                localDb.addPlaylistQueries.updatePlaylistId(
                                    playlistId = response.playlistId,
                                    playlistName = payload.name
                                )
                            }
                        }
                        response != null && response.error == null
                    }
                    "PLAYLIST_UPDATE" -> {
                        val payload = Json.decodeFromString(
                            PlaylistUpdateTaskPayload.serializer(),
                            task.payload ?: ""
                        )
                        val response = apiClient.updatePlaylistSongs(
                            playlistId = task.targetId,
                            songList = payload.songs
                        )
                        response != null && response.error == null
                    }
                    "PLAYLIST_DELETE" -> {
                        val response = apiClient.deletePlaylist(
                            playlistId = task.targetId
                        )
                        response != null && response.error == null
                    }
                    "PLAYLIST_DETAILS_UPDATE" -> {
                        val payload = Json.decodeFromString(
                            PlaylistDetailsUpdatePayload.serializer(),
                            task.payload ?: ""
                        )
                        val response = apiClient.updatePlaylistDetails(
                            playlistId = task.targetId,
                            name = payload.name,
                            description = payload.description
                        )
                        response != null && response.error == null
                    }
                    else -> true
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                when {
                    task.actionType == "FAVORITE" && e is ApiRequestException && e.statusCode == 409 -> true
                    task.actionType == "UNFAVORITE" && e is ApiRequestException &&
                        (e.statusCode == 404 || e.statusCode == 409) -> true
                    else -> false
                }
            }

            val shouldContinue = authSessionLock.withLock {
                session.requireActive(secureStorage)
                if (success) {
                    if (task.actionType == "FAVORITE" || task.actionType == "UNFAVORITE") {
                        updateBaselineAfterSuccessfulMutation(
                            userId = userId,
                            trackId = task.targetId,
                            isFavorite = task.actionType == "FAVORITE",
                        )
                    }
                    localDb.syncQueueQueries.deleteTask(task.id)
                } else {
                    val errMsg = "Sync failed at task: ${task.actionType} for id ${task.targetId}"
                    localDb.syncQueueQueries.updateTaskError(
                        errorMessage = errMsg,
                        retryCount = task.retryCount + 1,
                        id = task.id
                    )
                    _syncState.value = SyncState.SyncFailed(errMsg)
                    scheduleRetry(task.retryCount + 1, manageDurableRetry)
                    return@withLock false
                }
                true
            }
            if (!shouldContinue) return false
        }
    }

    private fun updateBaselineAfterSuccessfulMutation(
        userId: String,
        trackId: String,
        isFavorite: Boolean,
    ) {
        val initialized = localDb.favoriteSyncQueries
            .isFavoriteSyncInitialized(userId)
            .executeAsOne()
        if (!initialized) return

        if (isFavorite) {
            localDb.favoriteSyncQueries.insertFavoriteSyncBaseline(userId, trackId)
        } else {
            localDb.favoriteSyncQueries.deleteFavoriteSyncBaseline(userId, trackId)
        }
    }

    override suspend fun performInboundSync(userId: String) {
        try {
            val session = SyncSession.capture(secureStorage, userId)
            withContext(ioDispatcher + session) {
                // Serialize remote snapshots with outbound writes; local taps only use
                // the short database/session critical section below.
                val shouldDrainQueue = mutex.withLock { reconcileFavoriteSnapshot(userId) }
                if (shouldDrainQueue) {
                    triggerSync()
                    retryScheduler.scheduleRetry(0L)
                }
                syncRemotePlaylists(userId)
            }
        } catch (_: SyncSessionChanged) {
            // A completed response from an old session must never replace this library.
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Logger.e(e) { "Error in performInboundSync" }
            _syncState.value = SyncState.SyncFailed("Inbound sync failed: ${e.message}")
        }
    }

    private suspend fun reconcileFavoriteSnapshot(userId: String): Boolean {
        val remoteTrackIds = apiClient.getUserFavorites(userId)
            .filter {
                it.type.equals("SaveType.track", ignoreCase = true) ||
                    it.type.equals("track", ignoreCase = true)
            }
            .map { it.itemId }.toSet()
        val identities = localDb.favoriteSyncQueries.getFavoriteTrackIdentities(userId)
            .executeAsList().associate { it.remoteTrackId to it.publicTrackId }.toMutableMap()
        val cachedTracks = remoteTrackIds.mapNotNull(identities::get).distinct().chunked(500)
            .flatMap { localDb.getCustomPlaylistSongs(it) }.associateBy { it.id }
        val idsNeedingMetadata = favoriteRemoteIdsNeedingMetadata(
            remoteTrackIds, identities, cachedTracks.keys,
        )
        val fetchedTracks = apiClient.getFavoriteTrackDetailsByRemoteId(idsNeedingMetadata)
        val snapshotComplete = idsNeedingMetadata.all { it in fetchedTracks }
        fetchedTracks.forEach { (remoteId, track) -> identities[remoteId] = track.id }
        val session = currentCoroutineContext()[SyncSession]!!

        // Read the latest local state and pending intent only after network I/O.
        // Favorite edits use this same short lock and a database transaction.
        return authSessionLock.withLock {
            session.requireActive(secureStorage)
            localDb.transactionWithResult {
                for ((remoteId, track) in fetchedTracks) {
                    localDb.favoriteSyncQueries.upsertFavoriteTrackIdentity(userId, remoteId, track.id)
                }
                val remoteFavorites = remoteTrackIds.mapNotNull(identities::get).toSet()
                val localTracks = localDb.getFavoritePlaylist().associateBy { it.id }
                val pendingFavorites = localDb.syncQueueQueries.getPendingFavoriteTasks(userId)
                    .executeAsList().associate { it.targetId to (it.actionType == "FAVORITE") }
                val initialized = localDb.favoriteSyncQueries.isFavoriteSyncInitialized(userId).executeAsOne()
                val baseline = if (initialized) {
                    localDb.favoriteSyncQueries.getFavoriteSyncBaseline(userId).executeAsList().toSet()
                } else null
                val previousUser = localDb.favoriteSyncQueries.getActiveFavoriteSyncUser()
                    .executeAsOneOrNull()?.activeUserId

                // An unresolved remote ID may refer to any local favorite. Until it
                // resolves, do not infer deletions or uploads from its absence.
                val plan = buildFavoriteSyncPlan(
                    localFavorites = localTracks.keys,
                    remoteFavorites = if (snapshotComplete) remoteFavorites else remoteFavorites + localTracks.keys,
                    baselineFavorites = baseline,
                    pendingFavorites = pendingFavorites,
                    remoteAuthoritative = previousUser != null && previousUser != userId,
                )
                val tracksById = (cachedTracks + fetchedTracks.values.associateBy { it.id }).toMutableMap()
                // Pending favorites from a prior login may already have metadata
                // in Library even when they are not in the remote favorite set.
                pendingFavorites.keys.chunked(500).flatMap { localDb.getCustomPlaylistSongs(it) }
                    .forEach { if (it.id !in tracksById) tracksById[it.id] = it }
                val tracksToAdd = plan.addLocally.intersect(tracksById.keys)
                for (trackId in plan.removeLocally) {
                    val track = localTracks.getValue(trackId)
                    localDb.saveSongToFavorites(
                        track.id, track.title, track.user, track.songImgList, "Favorite", false,
                    )
                }
                for (trackId in tracksToAdd) {
                    val track = tracksById.getValue(trackId)
                    localDb.saveSongToFavorites(
                        track.id, track.title, track.user, track.songImgList, "Favorite", true,
                    )
                }

                // Keep explicit actions for unresolved IDs; an incomplete read
                // cannot acknowledge or cancel a user's pending write.
                val unresolvedPending = if (snapshotComplete) emptySet() else pendingFavorites.keys - remoteFavorites
                val normalizedIds = (pendingFavorites.keys + plan.favoriteRemotely +
                    plan.unfavoriteRemotely) - unresolvedPending
                for (trackId in normalizedIds) {
                    localDb.syncQueueQueries.deleteFavoriteTasksByTarget(userId, trackId)
                }
                for (trackId in plan.favoriteRemotely - unresolvedPending) insertFavoriteTask(userId, trackId, true)
                for (trackId in plan.unfavoriteRemotely - unresolvedPending) insertFavoriteTask(userId, trackId, false)

                val appliedCompleteSnapshot = snapshotComplete && tracksToAdd == plan.addLocally
                if (appliedCompleteSnapshot) {
                    localDb.favoriteSyncQueries.clearFavoriteSyncBaseline(userId)
                    plan.desiredFavorites.forEach {
                        localDb.favoriteSyncQueries.insertFavoriteSyncBaseline(userId, it)
                    }
                    localDb.favoriteSyncQueries.markFavoriteSyncInitialized(userId)
                    localDb.favoriteSyncQueries.setActiveFavoriteSyncUser(userId)
                }
                val pendingCount = localDb.syncQueueQueries.getPendingTasks(userId).executeAsList().size
                _syncState.value = when {
                    !appliedCompleteSnapshot -> SyncState.SyncFailed(
                        "Some favorite details are unavailable; they will be retried on the next sync",
                    )
                    pendingCount > 0 -> SyncState.Syncing(pendingCount)
                    else -> SyncState.Synced
                }
                pendingCount > 0
            }
        }
    }

    private suspend fun syncRemotePlaylists(userId: String) {
        val remotePlaylists = try {
            apiClient.getUserPlaylists(userId)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Logger.e(e) { "Error fetching user playlists" }
            return
        }
        val snapshots = remotePlaylists.chunked(3).flatMap { chunk ->
            coroutineScope {
                chunk.map { playlist ->
                    async {
                        try {
                            val response = apiClient.getResponse<com.rld.justlisten.datalayer.webservices.apis.playlistcalls.PlayListResponse>(
                                "/playlists/${playlist.id}/tracks"
                            )
                            playlist to response?.data.orEmpty()
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            Logger.e(e) { "Error syncing playlist ${playlist.id}" }
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
            }
        }
        val session = currentCoroutineContext()[SyncSession]!!
        for ((playlist, tracks) in snapshots) {
            val playlistName = playlist.playlistTitle.ifBlank { playlist.title }
            if (playlistName.isBlank()) continue
            authSessionLock.withLock {
                session.requireActive(secureStorage)
                localDb.transaction {
                    for (track in tracks) {
                        if (localDb.libraryQueries.getSongWithId(track.id).executeAsOneOrNull() == null) {
                            localDb.saveSongToFavorites(
                                id = track.id,
                                title = track.title,
                                user = track.user,
                                songImgList = track.songImgList,
                                playlistName = playlistName,
                                isFavorite = false,
                            )
                        }
                    }
                    localDb.updatePlaylistSongs(
                        playlistName = playlistName,
                        playlistDescription = null,
                        songList = tracks.map { it.id },
                        isRemote = true,
                        isPrivate = false,
                        playlistId = playlist.id,
                    )
                }
            }
        }
    }

    private fun insertFavoriteTask(userId: String, trackId: String, isFavorite: Boolean) {
        localDb.syncQueueQueries.insertTask(
            actionType = if (isFavorite) "FAVORITE" else "UNFAVORITE",
            targetId = trackId,
            userId = userId,
            payload = null,
            errorMessage = null,
            retryCount = 0,
        )
    }
}
