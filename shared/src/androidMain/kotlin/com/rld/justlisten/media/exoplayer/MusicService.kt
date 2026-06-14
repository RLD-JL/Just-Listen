package com.rld.justlisten.media.exoplayer

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.rld.justlisten.media.exoplayer.utils.Constants.NETWORK_ERROR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import org.koin.android.ext.android.inject

import android.content.Context
import android.app.ActivityManager
import androidx.media3.exoplayer.DefaultLoadControl
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import com.rld.justlisten.viewmodel.settings.SettingsViewModel

private const val SERVICE_TAG = "MusicService"

class MusicService : MediaSessionService() {

    private var exoPlayer: ExoPlayer? = null
    private var secondaryExoPlayer: ExoPlayer? = null
    private var currentPlayer: ExoPlayer? = null
    private var isCrossfading = false
    private var equalizer: android.media.audiofx.Equalizer? = null
    private var equalizerSessionId: Int = -1
    private var audioAttributes: AudioAttributes? = null

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var progressMonitorJob: Job? = null
    private var crossfadeJob: Job? = null

    private var mediaSession: MediaSession? = null

    val musicSource: MusicSource by inject()
    val settingsRepository: com.rld.justlisten.datalayer.repositories.SettingsRepository by inject()

    override fun onCreate() {
        super.onCreate()

        val cacheDataSourceFactory: androidx.media3.datasource.cache.CacheDataSource.Factory by inject()
        val attrs: androidx.media3.common.AudioAttributes by inject()
        audioAttributes = attrs

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(androidx.media3.exoplayer.source.DefaultMediaSourceFactory(cacheDataSourceFactory))
            .build().apply {
                setAudioAttributes(attrs, true)
                setHandleAudioBecomingNoisy(true)
            }
        exoPlayer = player
        currentPlayer = player

        // Optimize buffer sizes for secondary player to conserve RAM, perform memory checks
        if (!isLowMemoryDevice()) {
            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    15000, // minBufferMs
                    20000, // maxBufferMs
                    1500,  // bufferForPlaybackMs
                    2000   // bufferForPlaybackAfterRebufferMs
                )
                .build()

            secondaryExoPlayer = ExoPlayer.Builder(this)
                .setMediaSourceFactory(androidx.media3.exoplayer.source.DefaultMediaSourceFactory(cacheDataSourceFactory))
                .setLoadControl(loadControl)
                .build().apply {
                    setAudioAttributes(attrs, true)
                    setHandleAudioBecomingNoisy(true)
                }
        }

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

        val sessionBuilder = MediaSession.Builder(this, player)
        if (activityIntent != null) {
            sessionBuilder.setSessionActivity(activityIntent)
        }
        mediaSession = sessionBuilder.build()

        startProgressMonitor()

        // Collect Equalizer settings and apply to native Equalizer
        val settingsViewModel: SettingsViewModel by inject()
        serviceScope.launch {
            settingsViewModel.settingsState.collect { state ->
                updateEqualizer(state.isEqEnabled, state.eqBands)
            }
        }
    }

    private fun isLowMemoryDevice(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val lowRam = memoryInfo.lowMemory || activityManager.isLowRamDevice
        val runtime = Runtime.getRuntime()
        val usedMemory = runtime.totalMemory() - runtime.freeMemory()
        val actualAvailableMemory = runtime.maxMemory() - usedMemory
        val lowFreeMem = actualAvailableMemory < 15 * 1024 * 1024L
        return lowRam || lowFreeMem
    }

    private fun startProgressMonitor() {
        progressMonitorJob?.cancel()
        progressMonitorJob = serviceScope.launch {
            while (isActive) {
                val player = currentPlayer
                if (player != null && player.isPlaying && !isCrossfading) {
                    val duration = player.duration
                    val position = player.currentPosition
                    if (duration > 0 && duration != C.TIME_UNSET) {
                        val remainingMs = duration - position
                        val crossfadeDurationMs = (settingsRepository.crossfadeDurationSeconds * 1000).toLong()
                        if (remainingMs <= crossfadeDurationMs && settingsRepository.isCrossfadeEnabled) {
                            val nextIndex = player.nextMediaItemIndex
                            if (nextIndex != C.INDEX_UNSET && nextIndex < player.mediaItemCount) {
                                startCrossfade(crossfadeDurationMs, nextIndex)
                            }
                        }
                    }
                }
                delay(200L)
            }
        }
    }

    private fun startCrossfade(durationMs: Long, nextIndex: Int) {
        val primary = currentPlayer ?: return
        val secondary = secondaryExoPlayer ?: return
        if (isLowMemoryDevice()) return

        isCrossfading = true

        val targetMediaId = primary.getMediaItemAt(nextIndex).mediaId

        // Disable audio focus handling temporarily on secondary player only,
        // so it doesn't request focus and pause the already playing primary player.
        val attrs = audioAttributes
        if (attrs != null) {
            secondary.setAudioAttributes(attrs, false)
        }

        // Copy playlist & playback configurations
        val mediaItems = (0 until primary.mediaItemCount).map { primary.getMediaItemAt(it) }
        secondary.setMediaItems(mediaItems)
        secondary.repeatMode = primary.repeatMode
        secondary.shuffleModeEnabled = primary.shuffleModeEnabled
        secondary.seekTo(nextIndex, 0L)
        secondary.volume = 0f
        secondary.prepare()
        secondary.play()

        // Remove subsequent items from primary player so it stops at the end of the current song
        // and doesn't transition to the next song, which would steal audio focus and pause playback.
        if (primary.currentMediaItemIndex + 1 < primary.mediaItemCount) {
            primary.removeMediaItems(primary.currentMediaItemIndex + 1, primary.mediaItemCount)
        }

        // Swap player roles in session immediately so UI/Session controls the new player
        mediaSession?.player = secondary
        currentPlayer = secondary
        secondaryExoPlayer = primary

        co.touchlab.kermit.Logger.d { "startCrossfade: primary=${primary.currentMediaItem?.mediaId}, secondary=${secondary.currentMediaItem?.mediaId}, targetMediaId=$targetMediaId, durationMs=$durationMs" }

        crossfadeJob = serviceScope.launch {
            val intervalMs = 100L
            val totalSteps = (durationMs / intervalMs).coerceAtLeast(1L)
            val initialMediaId = targetMediaId

            co.touchlab.kermit.Logger.d { "startCrossfade loop start: initialMediaId=$initialMediaId, playWhenReady=${secondary.playWhenReady}" }

            for (step in 1..totalSteps) {
                val currentId = secondary.currentMediaItem?.mediaId
                if (!secondary.playWhenReady || (currentId != null && currentId != initialMediaId)) {
                    co.touchlab.kermit.Logger.w { "startCrossfade ABORTED at step $step: playWhenReady=${secondary.playWhenReady}, currentMediaId=$currentId, initialMediaId=$initialMediaId" }
                    // Restore audio focus handling on abort
                    val restoreAttrs = audioAttributes
                    if (restoreAttrs != null) {
                        secondary.setAudioAttributes(restoreAttrs, true)
                    }
                    // Abort if active playback stops or song is skipped manually
                    primary.volume = 1f
                    secondary.volume = 1f
                    primary.pause()
                    isCrossfading = false
                    return@launch
                }
                val progress = step.toFloat() / totalSteps
                primary.volume = 1f - progress
                secondary.volume = progress
                co.touchlab.kermit.Logger.v { "startCrossfade step $step/$totalSteps: primaryVol=${primary.volume}, secondaryVol=${secondary.volume}" }
                delay(intervalMs)
            }

            // Finalize crossfade
            primary.pause()
            primary.volume = 1f
            secondary.volume = 1f

            // Restore audio focus handling on success
            val restoreAttrs = audioAttributes
            if (restoreAttrs != null) {
                secondary.setAudioAttributes(restoreAttrs, true)
            }

            co.touchlab.kermit.Logger.d { "startCrossfade FINISHED successfully" }

            // Re-apply equalizer settings to the new active player's session
            val settingsViewModel: SettingsViewModel by inject()
            updateEqualizer(settingsViewModel.settingsState.value.isEqEnabled, settingsViewModel.settingsState.value.eqBands)

            isCrossfading = false
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player?.playWhenReady == false || player?.mediaItemCount == 0) {
            stopSelf()
        }
    }

    private fun updateEqualizer(enabled: Boolean, bands: List<Float>) {
        try {
            val player = currentPlayer ?: return
            val sessionId = player.audioSessionId
            if (sessionId == androidx.media3.common.C.AUDIO_SESSION_ID_UNSET) {
                equalizer?.release()
                equalizer = null
                return
            }

            var eq = equalizer
            if (eq == null || equalizerSessionId != sessionId) {
                equalizer?.release()
                eq = android.media.audiofx.Equalizer(0, sessionId)
                equalizer = eq
                equalizerSessionId = sessionId
            }

            eq.enabled = enabled
            if (enabled) {
                val numBands = eq.numberOfBands.toInt()
                for (i in 0 until numBands) {
                    if (i < bands.size) {
                        val levelMillibels = (bands[i] * 100).toInt().coerceIn(-1500, 1500).toShort()
                        eq.setBandLevel(i.toShort(), levelMillibels)
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("MusicService", "Error updating native Equalizer: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        progressMonitorJob?.cancel()
        crossfadeJob?.cancel()
        mediaSession?.run {
            release()
            mediaSession = null
        }
        equalizer?.release()
        equalizer = null
        exoPlayer?.release()
        exoPlayer = null
        secondaryExoPlayer?.release()
        secondaryExoPlayer = null
        currentPlayer = null
        serviceScope.cancel()
        super.onDestroy()
    }
}
