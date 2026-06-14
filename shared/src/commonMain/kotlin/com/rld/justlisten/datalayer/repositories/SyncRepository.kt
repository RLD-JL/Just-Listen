package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.localdb.libraryscreen.saveSongToFavorites
import com.rld.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylist
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserFavorites
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserFavoriteTracks
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserPlaylists
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getTrackDetails
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.updatePlaylistSongs
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import co.touchlab.kermit.Logger

sealed interface SyncState {
    object Synced : SyncState
    data class Syncing(val pendingCount: Int) : SyncState
    data class SyncFailed(val errorMessage: String) : SyncState
}

interface SyncRepository {
    val syncState: StateFlow<SyncState>
    fun enqueueFavoriteTask(trackId: String, isFavorite: Boolean)
    fun enqueuePlaylistCreateTask(name: String, description: String?, isPrivate: Boolean)
    fun enqueuePlaylistUpdateTask(playlistId: String, songs: List<String>)
    fun enqueuePlaylistDeleteTask(playlistId: String)
    fun enqueuePlaylistDetailsUpdateTask(playlistId: String, name: String, description: String?)
    fun triggerSync()
    fun clearQueue()
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
    private val apiClient: ApiClient
) : SyncRepository {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Synced)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var syncJob: Job? = null
    private val mutex = kotlinx.coroutines.sync.Mutex()

    init {
        triggerSync()
    }

    override fun enqueueFavoriteTask(trackId: String, isFavorite: Boolean) {
        val actionType = if (isFavorite) "FAVORITE" else "UNFAVORITE"
        val existingTasks = localDb.syncQueueQueries.getTasksByTarget(trackId).executeAsList()
        
        if (existingTasks.isNotEmpty()) {
            val oppositeAction = if (isFavorite) "UNFAVORITE" else "FAVORITE"
            val hasOpposite = existingTasks.any { it.actionType == oppositeAction }
            if (hasOpposite) {
                // They cancel each other out. Remove all pending tasks for this track.
                localDb.syncQueueQueries.deleteTasksByTarget(trackId)
                return
            } else {
                // Duplicate task already exists in the queue, ignore.
                return
            }
        }

        localDb.syncQueueQueries.insertTask(
            actionType = actionType,
            targetId = trackId,
            payload = null,
            errorMessage = null,
            retryCount = 0
        )
        triggerSync()
    }

    override fun enqueuePlaylistCreateTask(name: String, description: String?, isPrivate: Boolean) {
        val payloadJson = Json.encodeToString(
            PlaylistTaskPayload.serializer(),
            PlaylistTaskPayload(name, description, isPrivate)
        )
        localDb.syncQueueQueries.insertTask(
            actionType = "PLAYLIST_CREATE",
            targetId = "NEW",
            payload = payloadJson,
            errorMessage = null,
            retryCount = 0
        )
        triggerSync()
    }

    override fun enqueuePlaylistUpdateTask(playlistId: String, songs: List<String>) {
        val payloadJson = Json.encodeToString(
            PlaylistUpdateTaskPayload.serializer(),
            PlaylistUpdateTaskPayload(songs)
        )
        localDb.syncQueueQueries.insertTask(
            actionType = "PLAYLIST_UPDATE",
            targetId = playlistId,
            payload = payloadJson,
            errorMessage = null,
            retryCount = 0
        )
        triggerSync()
    }

    override fun enqueuePlaylistDeleteTask(playlistId: String) {
        localDb.syncQueueQueries.insertTask(
            actionType = "PLAYLIST_DELETE",
            targetId = playlistId,
            payload = null,
            errorMessage = null,
            retryCount = 0
        )
        triggerSync()
    }

    override fun enqueuePlaylistDetailsUpdateTask(playlistId: String, name: String, description: String?) {
        val payloadJson = Json.encodeToString(
            PlaylistDetailsUpdatePayload.serializer(),
            PlaylistDetailsUpdatePayload(name, description)
        )
        localDb.syncQueueQueries.insertTask(
            actionType = "PLAYLIST_DETAILS_UPDATE",
            targetId = playlistId,
            payload = payloadJson,
            errorMessage = null,
            retryCount = 0
        )
        triggerSync()
    }


    override fun triggerSync() {
        if (syncJob?.isActive == true) return
        syncJob = scope.launch {
            if (mutex.tryLock()) {
                try {
                    processQueue()
                } finally {
                    mutex.unlock()
                }
            }
        }
    }

    override fun clearQueue() {
        syncJob?.cancel()
        val pending = localDb.syncQueueQueries.getPendingTasks().executeAsList()
        pending.forEach {
            localDb.syncQueueQueries.deleteTask(it.id)
        }
        _syncState.value = SyncState.Synced
    }

    private suspend fun processQueue() {
        while (true) {
            val pendingTasks = localDb.syncQueueQueries.getPendingTasks().executeAsList()
            if (pendingTasks.isEmpty()) {
                _syncState.value = SyncState.Synced
                break
            }

            _syncState.value = SyncState.Syncing(pendingTasks.size)

            val task = pendingTasks.first()

            if (task.retryCount >= 3) {
                Logger.e { "Skipping task ${task.id} (${task.actionType} for ${task.targetId}) as it exceeded max retry limit of 3. Error: ${task.errorMessage}" }
                localDb.syncQueueQueries.deleteTask(task.id)
                continue
            }
            
            // Stay within rate limits by introducing a 200ms delay between actions
            delay(200L)

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
                            localDb.addPlaylistQueries.updatePlaylistId(
                                playlistId = response.playlistId,
                                playlistName = payload.name
                            )
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
                false
            }

            if (success) {
                localDb.syncQueueQueries.deleteTask(task.id)
            } else {
                val errMsg = "Sync failed at task: ${task.actionType} for id ${task.targetId}"
                localDb.syncQueueQueries.updateTaskError(
                    errorMessage = errMsg,
                    retryCount = task.retryCount + 1,
                    id = task.id
                )
                _syncState.value = SyncState.SyncFailed(errMsg)
                break
            }
        }
    }

    override suspend fun performInboundSync(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                // 1. Cheap check: fetch lightweight favorites list (single request, numeric IDs)
                val cloudFavoritesList = apiClient.getUserFavorites(userId)
                val cloudNumericIds = cloudFavoritesList
                    .filter { it.type.equals("SaveType.track", ignoreCase = true) || it.type.equals("track", ignoreCase = true) }
                    .map { it.itemId }
                    .toSet()

                // 2. Get local favorites (base58 IDs)
                val localFavorites = localDb.getFavoritePlaylist().map { it.id }.toSet()

                // 3. Quick comparison: if cloud count matches local count and no pending
                //    outbound tasks, skip expensive per-track fetches entirely
                val pendingFavTasks = localDb.syncQueueQueries.getPendingTasks().executeAsList()
                    .filter { it.actionType == "FAVORITE" || it.actionType == "UNFAVORITE" }
                val hasPendingFavChanges = pendingFavTasks.isNotEmpty()

                if (cloudNumericIds.size == localFavorites.size && !hasPendingFavChanges) {
                    Logger.d { "SyncRepository: Favorites in sync (${localFavorites.size} local, ${cloudNumericIds.size} cloud). Skipping track fetches." }
                } else {
                    Logger.d { "SyncRepository: Favorites differ (${localFavorites.size} local, ${cloudNumericIds.size} cloud). Fetching track details..." }
                    // Expensive path: fetch full track details to get base58 IDs
                    val cloudFavoriteTracks = apiClient.getUserFavoriteTracks(userId)
                    val cloudFavorites = cloudFavoriteTracks.map { it.id }.toSet()

                    // Cloud -> Local (Download missing favorites)
                    val missingLocally = cloudFavorites - localFavorites
                    if (missingLocally.isNotEmpty()) {
                        _syncState.value = SyncState.Syncing(missingLocally.size)

                        val fetchedTracks = cloudFavoriteTracks.filter { it.id in missingLocally }

                        if (fetchedTracks.isNotEmpty()) {
                            localDb.transaction {
                                for (track in fetchedTracks) {
                                    localDb.saveSongToFavorites(
                                        id = track.id,
                                        title = track.title,
                                        user = track.user,
                                        songImgList = track.songImgList,
                                        playlistName = "Favorite",
                                        isFavorite = true
                                    )
                                }
                            }
                        }
                    }

                    // Local -> Cloud (Upload missing favorites)
                    val missingInCloud = localFavorites - cloudFavorites
                    for (trackId in missingInCloud) {
                        enqueueFavoriteTask(trackId, isFavorite = true)
                    }
                }

                // 5. Get cloud playlists and sync them
                try {
                    val remotePlaylists = apiClient.getUserPlaylists(userId)
                    for (playlist in remotePlaylists) {
                        try {
                            val playlistName = playlist.playlistTitle.ifBlank { playlist.title }
                            if (playlistName.isNotBlank()) {
                                // Fetch tracks for this playlist
                                val tracksResponse = apiClient.getResponse<com.rld.justlisten.datalayer.webservices.apis.playlistcalls.PlayListResponse>("/playlists/${playlist.id}/tracks")
                                val tracksList = tracksResponse?.data ?: emptyList()
                                val songList = tracksList.map { it.id }
                                
                                // Insert songs details to Library table if they don't exist
                                localDb.transaction {
                                    for (track in tracksList) {
                                        val existing = localDb.libraryQueries.getSongWithId(track.id).executeAsOneOrNull()
                                        if (existing == null) {
                                            localDb.saveSongToFavorites(
                                                id = track.id,
                                                title = track.title,
                                                user = track.user,
                                                songImgList = track.songImgList,
                                                playlistName = playlistName,
                                                isFavorite = false
                                            )
                                        }
                                    }
                                }
                                
                                // Save playlist definition
                                localDb.updatePlaylistSongs(
                                    playlistName = playlistName,
                                    playlistDescription = null,
                                    songList = songList,
                                    isRemote = true,
                                    isPrivate = false,
                                    playlistId = playlist.id
                                )
                            }
                            delay(100L) // rate-limiting friendly
                        } catch (e: Exception) {
                            if (e is CancellationException) throw e
                            Logger.e(e) { "Error syncing playlist ${playlist.id}" }
                        }
                    }
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    Logger.e(e) { "Error fetching user playlists" }
                }

                _syncState.value = SyncState.Synced
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.e(e) { "Error in performInboundSync" }
                _syncState.value = SyncState.SyncFailed("Inbound sync failed: ${e.message}")
            }
        }
    }
}

