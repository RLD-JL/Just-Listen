package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.localdb.libraryscreen.saveSongToFavorites
import com.rld.justlisten.datalayer.localdb.libraryscreen.getFavoritePlaylist
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserFavorites
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserPlaylists
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getTrackDetails
import com.rld.justlisten.datalayer.localdb.addplaylistscreen.updatePlaylistSongs
import com.rld.justlisten.datalayer.webservices.apis.writecalls.favoriteTrack
import com.rld.justlisten.datalayer.webservices.apis.writecalls.unfavoriteTrack
import com.rld.justlisten.datalayer.webservices.apis.writecalls.createPlaylist
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

sealed interface SyncState {
    object Synced : SyncState
    data class Syncing(val pendingCount: Int) : SyncState
    data class SyncFailed(val errorMessage: String) : SyncState
}

interface SyncRepository {
    val syncState: StateFlow<SyncState>
    fun enqueueFavoriteTask(trackId: String, isFavorite: Boolean)
    fun enqueuePlaylistCreateTask(name: String, description: String?, isPrivate: Boolean)
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
            
            // Stay within rate limits by introducing a 200ms delay between actions
            delay(200L)

            val success = try {
                when (task.actionType) {
                    "FAVORITE" -> {
                        val response = apiClient.favoriteTrack(task.targetId)
                        response?.error == null
                    }
                    "UNFAVORITE" -> {
                        val response = apiClient.unfavoriteTrack(task.targetId)
                        response?.error == null
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
                        response?.error == null
                    }
                    else -> true
                }
            } catch (e: Exception) {
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
                // 1. Get cloud favorites
                val cloudFavorites = apiClient.getUserFavorites(userId)
                    .filter { it.type == "SaveType.track" || it.type == "track" }
                    .map { it.itemId }
                    .toSet()

                // 2. Get local favorites
                val localFavorites = localDb.getFavoritePlaylist().map { it.id }.toSet()

                // 3. Cloud -> Local (Download missing favorites)
                val missingLocally = cloudFavorites - localFavorites
                if (missingLocally.isNotEmpty()) {
                    _syncState.value = SyncState.Syncing(missingLocally.size)
                    
                    val fetchedTracks = mutableListOf<PlayListModel>()
                    for (trackId in missingLocally) {
                        try {
                            val track = apiClient.getTrackDetails(trackId)
                            if (track != null) {
                                fetchedTracks.add(track)
                            }
                            delay(100L) // rate-limiting friendly
                        } catch (e: Exception) {
                            println("Error fetching track details for $trackId: ${e.message}")
                        }
                    }

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

                // 4. Local -> Cloud (Upload missing favorites)
                val missingInCloud = localFavorites - cloudFavorites
                for (trackId in missingInCloud) {
                    enqueueFavoriteTask(trackId, isFavorite = true)
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
                                    isPrivate = false
                                )
                            }
                            delay(100L) // rate-limiting friendly
                        } catch (e: Exception) {
                            println("Error syncing playlist ${playlist.id}: ${e.message}")
                        }
                    }
                } catch (e: Exception) {
                    println("Error fetching user playlists: ${e.message}")
                }

                _syncState.value = SyncState.Synced
            } catch (e: Exception) {
                println("Error in performInboundSync: ${e.message}")
                _syncState.value = SyncState.SyncFailed("Inbound sync failed: ${e.message}")
            }
        }
    }
}

