package com.rld.justlisten.media

import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.models.SongIconList
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collectLatest

class PlayHistoryTracker(
    private val libraryRepository: LibraryRepository,
    private val musicPlayer: MusicPlayer,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {
    private var job: Job? = null

    init {
        startTracking()
    }

    fun startTracking() {
        job?.cancel()
        job = scope.launch {
            var lastActiveSongId: String? = null
            var currentSongPlayTimeMs = 0L
            var playCompletedLogged = false
            var lastStatus: PlaybackStatus? = null

            // 1. Observe state changes (flow collection) to handle song changes and pause/stop events.
            // Using standard collect to avoid cancellation on frequent progress ticks.
            launch {
                musicPlayer.playbackState.collect { state ->
                    val media = state.currentMedia
                    val songId = media?.id

                    if (songId != lastActiveSongId) {
                        // Song changed: save previous song's accumulated duration
                        val activeSongId = lastActiveSongId
                        if (activeSongId != null && currentSongPlayTimeMs > 0) {
                            saveIncrementalDuration(activeSongId, currentSongPlayTimeMs, playCompletedLogged)
                        }
                        
                        // Automatically upsert the new song into Library recents to guarantee metadata exists for JOIN queries
                        if (media != null) {
                            scope.launch(Dispatchers.IO) {
                                libraryRepository.saveSongToRecent(
                                    id = media.id,
                                    title = media.title,
                                    user = UserModel(username = media.artist, id = media.artistId),
                                    songImgList = SongIconList(
                                        songImageURL150px = media.lowResArtworkUrl ?: "",
                                        songImageURL480px = media.artworkUrl ?: ""
                                    ),
                                    playlistName = ""
                                )
                            }
                        }
                        
                        lastActiveSongId = songId
                        currentSongPlayTimeMs = 0L
                        playCompletedLogged = false
                    } else if (state.status != PlaybackStatus.PLAYING && lastStatus == PlaybackStatus.PLAYING) {
                        // Transitioned from PLAYING to another state (paused/stopped): flush duration immediately
                        val activeSongId = lastActiveSongId
                        if (activeSongId != null && currentSongPlayTimeMs > 0) {
                            saveIncrementalDuration(activeSongId, currentSongPlayTimeMs, playCompletedLogged)
                            currentSongPlayTimeMs = 0L
                        }
                    }
                    lastStatus = state.status
                }
            }

            // 2. Periodic 1-second timer to accumulate duration during active playback.
            // This runs independently from flow emissions to prevent resetting when position ticks are received.
            while (isActive) {
                delay(1000)

                val state = musicPlayer.playbackState.value
                val media = state.currentMedia
                val songId = media?.id

                if (state.status == PlaybackStatus.PLAYING && songId != null) {
                    currentSongPlayTimeMs += 1000L

                    val duration = media.duration
                    val thresholdMs = if (duration > 0) {
                        val fiftyPercent = duration / 2
                        if (fiftyPercent < 30000L) fiftyPercent else 30000L
                    } else {
                        30000L
                    }

                    if (currentSongPlayTimeMs >= thresholdMs && !playCompletedLogged) {
                        playCompletedLogged = true
                        savePlaySession(songId, true)
                    }
                }
            }
        }
    }

    private fun savePlaySession(songId: String, completed: Boolean) {
        val timestamp = clockNowSeconds()
        scope.launch(Dispatchers.IO) {
            libraryRepository.insertPlayLog(
                songId = songId,
                timestamp = timestamp,
                durationPlayedSec = 0L,
                completed = completed
            )
        }
    }

    private fun saveIncrementalDuration(songId: String, playTimeMs: Long, completed: Boolean) {
        val durationSec = playTimeMs / 1000L
        if (durationSec > 0) {
            val timestamp = clockNowSeconds()
            scope.launch(Dispatchers.IO) {
                libraryRepository.insertPlayLog(
                    songId = songId,
                    timestamp = timestamp,
                    durationPlayedSec = durationSec,
                    completed = false // Only log completion when threshold is crossed in savePlaySession
                )
            }
        }
    }

    private fun clockNowSeconds(): Long {
        return kotlin.time.Clock.System.now().epochSeconds
    }

    fun release() {
        scope.cancel()
    }
}
