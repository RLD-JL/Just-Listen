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
    private var crossfadeTargetVolume = 1f
    private var lastPrimarySetVolume: Float? = null
    private var lastSecondarySetVolume: Float? = null

    private fun setPrimaryVolume(vol: Float) {
        val p = currentPlayer ?: return
        lastPrimarySetVolume = vol
        p.volume = vol
    }

    private fun setSecondaryVolume(vol: Float) {
        val s = secondaryExoPlayer ?: return
        lastSecondarySetVolume = vol
        s.volume = vol
    }

    private var equalizer: android.media.audiofx.Equalizer? = null
    private var equalizerSessionId: Int = -1
    private var secondaryEqualizer: android.media.audiofx.Equalizer? = null
    private var secondaryEqualizerSessionId: Int = -1
    private var dynamicsProcessing: android.media.audiofx.DynamicsProcessing? = null
    private var dynamicsProcessingSessionId: Int = -1
    private var secondaryDynamicsProcessing: android.media.audiofx.DynamicsProcessing? = null
    private var secondaryDynamicsProcessingSessionId: Int = -1
    private var audioAttributes: AudioAttributes? = null
    private var audioFocusRequest: android.media.AudioFocusRequest? = null
    private var wasPlayingBeforeFocusLoss = false

    private val audioFocusChangeListener = android.media.AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            android.media.AudioManager.AUDIOFOCUS_GAIN -> {
                currentPlayer?.let { p ->
                    p.volume = crossfadeTargetVolume
                    if (wasPlayingBeforeFocusLoss) {
                        p.play()
                    }
                }
            }
            android.media.AudioManager.AUDIOFOCUS_LOSS -> {
                wasPlayingBeforeFocusLoss = false
                currentPlayer?.pause()
            }
            android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                wasPlayingBeforeFocusLoss = currentPlayer?.playWhenReady == true
                currentPlayer?.pause()
            }
            android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                currentPlayer?.let { p ->
                    p.volume = crossfadeTargetVolume * 0.2f
                }
            }
        }
    }

    private fun requestAudioFocus(): Boolean {
        val audioManager = getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val playbackAttributes = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val request = android.media.AudioFocusRequest.Builder(android.media.AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(playbackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()
            audioFocusRequest = request
            audioManager.requestAudioFocus(request) == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                android.media.AudioManager.STREAM_MUSIC,
                android.media.AudioManager.AUDIOFOCUS_GAIN
            ) == android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonAudioFocus() {
        val audioManager = getSystemService(android.content.Context.AUDIO_SERVICE) as android.media.AudioManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
    }

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

        val volumeListener = object : Player.Listener {
            override fun onVolumeChanged(volume: Float) {
                val isInternal = (lastPrimarySetVolume != null && kotlin.math.abs(lastPrimarySetVolume!! - volume) < 0.001f) ||
                                 (lastSecondarySetVolume != null && kotlin.math.abs(lastSecondarySetVolume!! - volume) < 0.001f)
                if (!isInternal) {
                    crossfadeTargetVolume = volume
                    co.touchlab.kermit.Logger.d { "External volume change detected: $volume" }
                }
            }

            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                val settingsViewModel: SettingsViewModel by inject()
                val state = settingsViewModel.settingsState.value
                val p = exoPlayer
                if (p != null && p.audioSessionId == audioSessionId) {
                    updateEqualizer(state.isEqEnabled, state.eqBands, p)
                    updateDynamicsProcessing(state.isVolumeNormalizationEnabled, p)
                }
                val s = secondaryExoPlayer
                if (s != null && s.audioSessionId == audioSessionId) {
                    updateEqualizer(state.isEqEnabled, state.eqBands, s)
                    updateDynamicsProcessing(state.isVolumeNormalizationEnabled, s)
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (playWhenReady) {
                    requestAudioFocus()
                } else {
                    val primaryPlaying = exoPlayer?.playWhenReady == true
                    val secondaryPlaying = secondaryExoPlayer?.playWhenReady == true
                    if (!primaryPlaying && !secondaryPlaying) {
                        abandonAudioFocus()
                    }
                }
            }
        }

        val player = ExoPlayer.Builder(this)
            .setMediaSourceFactory(androidx.media3.exoplayer.source.DefaultMediaSourceFactory(cacheDataSourceFactory))
            .build().apply {
                setAudioAttributes(attrs, false)
                setHandleAudioBecomingNoisy(true)
            }
        player.addListener(volumeListener)
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
                    setAudioAttributes(attrs, false)
                    setHandleAudioBecomingNoisy(true)
                }
            secondaryExoPlayer?.addListener(volumeListener)
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
                updateDynamicsProcessing(state.isVolumeNormalizationEnabled)
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
                        val triggerDurationMs = if (settingsRepository.crossfadeStyle == "Radio Segue") {
                            (crossfadeDurationMs * 1.5).toLong()
                        } else {
                            crossfadeDurationMs
                        }
                        if (remainingMs <= triggerDurationMs && settingsRepository.isCrossfadeEnabled) {
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
        crossfadeTargetVolume = primary.volume
        lastPrimarySetVolume = crossfadeTargetVolume
        lastSecondarySetVolume = 0f

        val targetMediaId = primary.getMediaItemAt(nextIndex).mediaId
        val startingMediaId = primary.currentMediaItem?.mediaId

        var crossfadeListener: Player.Listener? = null

        fun abort() {
            if (!isCrossfading) return
            
            crossfadeListener?.let {
                primary.removeListener(it)
            }
            
            crossfadeJob?.cancel()
            crossfadeJob = null
            
            setPrimaryVolume(crossfadeTargetVolume)
            setSecondaryVolume(crossfadeTargetVolume)
            secondary.pause()
            isCrossfading = false
        }

        val listener = object : Player.Listener {
            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    co.touchlab.kermit.Logger.d { "Crossfade cancelled due to seek" }
                    abort()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                if (primary.currentMediaItem?.mediaId != startingMediaId) {
                    co.touchlab.kermit.Logger.d { "Crossfade cancelled due to media item transition" }
                    abort()
                }
            }

            override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                if (!playWhenReady) {
                    co.touchlab.kermit.Logger.d { "Crossfade paused/cancelled due to playWhenReadyChanged" }
                    abort()
                }
            }
        }
        crossfadeListener = listener

        primary.addListener(listener)

        // Copy playlist & playback configurations
        val mediaItems = (0 until primary.mediaItemCount).map { primary.getMediaItemAt(it) }
        secondary.setMediaItems(mediaItems)
        secondary.repeatMode = primary.repeatMode
        secondary.shuffleModeEnabled = primary.shuffleModeEnabled
        secondary.seekTo(nextIndex, 0L)
        setSecondaryVolume(0f)
        secondary.prepare()

        // Apply equalizer settings to secondary player immediately so it starts with correct timbre
        val settingsViewModel: SettingsViewModel by inject()
        updateEqualizer(settingsViewModel.settingsState.value.isEqEnabled, settingsViewModel.settingsState.value.eqBands, secondary)
        updateDynamicsProcessing(settingsViewModel.settingsState.value.isVolumeNormalizationEnabled, secondary)

        // We do NOT swap player roles in the mediaSession/currentPlayer here.
        // The playerbar and controls continue to show and target the previous song (primary).

        co.touchlab.kermit.Logger.d { "startCrossfade: primary=${primary.currentMediaItem?.mediaId}, secondary=${secondary.currentMediaItem?.mediaId}, targetMediaId=$targetMediaId, durationMs=$durationMs" }

        val style = settingsRepository.crossfadeStyle
        val intervalMs = 100L
        val totalDurationMs = if (style == "Radio Segue") (durationMs * 1.5).toLong() else durationMs
        val totalSteps = (totalDurationMs / intervalMs).coerceAtLeast(1L)
        val baseSteps = (durationMs / intervalMs).coerceAtLeast(1L)
        val halfSteps = if (style == "Radio Segue") baseSteps / 2 else totalSteps / 2
        var secondaryStarted = false

        if (style == "Smooth Blend") {
            secondary.play()
            secondaryStarted = true
        }

        crossfadeJob = serviceScope.launch {
            co.touchlab.kermit.Logger.d { "startCrossfade loop start: targetMediaId=$targetMediaId, style=$style, playWhenReady=${secondary.playWhenReady}" }

            for (step in 1..totalSteps) {
                val currentPrimaryId = primary.currentMediaItem?.mediaId
                // Abort if active playback (primary) stops, secondary stops (if started), or song is skipped/changed manually
                if (!primary.playWhenReady || (secondaryStarted && !secondary.playWhenReady) || currentPrimaryId != startingMediaId) {
                    co.touchlab.kermit.Logger.w { "startCrossfade ABORTED at step $step: primaryPlayWhenReady=${primary.playWhenReady}, secondaryPlayWhenReady=${secondary.playWhenReady}, currentPrimaryId=$currentPrimaryId, startingMediaId=$startingMediaId" }
                    abort()
                    return@launch
                }

                val duration = primary.duration
                if (duration > 0 && duration != C.TIME_UNSET) {
                    val remaining = duration - primary.currentPosition
                    if (remaining <= 250) {
                        co.touchlab.kermit.Logger.d { "Breaking crossfade loop early at step $step/$totalSteps to prevent STATE_ENDED. remaining=$remaining" }
                        break
                    }
                }
                
                var primaryVol = crossfadeTargetVolume
                var secondaryVol = 0f

                when (style) {
                    "Radio Segue" -> {
                        primaryVol = if (step > baseSteps) {
                            val progressPrimary = (step - baseSteps).toFloat() / (totalSteps - baseSteps)
                            val anglePrimary = progressPrimary * (kotlin.math.PI.toFloat() / 2f)
                            crossfadeTargetVolume * kotlin.math.cos(anglePrimary)
                        } else {
                            crossfadeTargetVolume
                        }

                        secondaryVol = if (step > halfSteps) {
                            if (!secondaryStarted) {
                                secondary.play()
                                secondaryStarted = true
                            }
                            if (step <= baseSteps) {
                                val progressSecondary = (step - halfSteps).toFloat() / (baseSteps - halfSteps)
                                val angleSecondary = progressSecondary * (kotlin.math.PI.toFloat() / 2f)
                                crossfadeTargetVolume * kotlin.math.sin(angleSecondary)
                            } else {
                                crossfadeTargetVolume
                            }
                        } else {
                            0f
                        }
                    }
                    "Compressed" -> {
                        if (step > halfSteps) {
                            if (!secondaryStarted) {
                                secondary.play()
                                secondaryStarted = true
                            }
                            val progress = (step - halfSteps).toFloat() / (totalSteps - halfSteps)
                            val angle = progress * (kotlin.math.PI.toFloat() / 2f)
                            primaryVol = crossfadeTargetVolume * kotlin.math.cos(angle)
                            secondaryVol = crossfadeTargetVolume * kotlin.math.sin(angle)
                        } else {
                            primaryVol = crossfadeTargetVolume
                            secondaryVol = 0f
                        }
                    }
                    else -> { // "Smooth Blend" (Traditional)
                        val progress = step.toFloat() / totalSteps
                        val angle = progress * (kotlin.math.PI.toFloat() / 2f)
                        primaryVol = crossfadeTargetVolume * kotlin.math.cos(angle)
                        secondaryVol = crossfadeTargetVolume * kotlin.math.sin(angle)
                    }
                }

                // Normalize volumes to ensure the sum never exceeds crossfadeTargetVolume
                val sumVol = primaryVol + secondaryVol
                if (sumVol > crossfadeTargetVolume && crossfadeTargetVolume > 0f) {
                    primaryVol = (primaryVol * crossfadeTargetVolume) / sumVol
                    secondaryVol = (secondaryVol * crossfadeTargetVolume) / sumVol
                }

                setPrimaryVolume(primaryVol)
                setSecondaryVolume(secondaryVol)
                
                co.touchlab.kermit.Logger.v { "startCrossfade step $step/$totalSteps: primaryVol=$primaryVol, secondaryVol=$secondaryVol" }
                delay(intervalMs)
            }

            // Remove listeners before final swap and cleanup to prevent any transition callbacks
            primary.removeListener(listener)
            secondary.removeListener(listener)

            // Finalize crossfade
            primary.pause()
            setPrimaryVolume(crossfadeTargetVolume)
            setSecondaryVolume(crossfadeTargetVolume)

            // Swap player roles in session now that the transition is complete
            mediaSession?.player = secondary
            currentPlayer = secondary
            secondaryExoPlayer = primary

            // Swap corresponding equalizer and dynamics processing variables to match the player swap
            val tempEq = equalizer
            val tempEqId = equalizerSessionId
            equalizer = secondaryEqualizer
            equalizerSessionId = secondaryEqualizerSessionId
            secondaryEqualizer = tempEq
            secondaryEqualizerSessionId = tempEqId

            val tempDp = dynamicsProcessing
            val tempDpId = dynamicsProcessingSessionId
            dynamicsProcessing = secondaryDynamicsProcessing
            dynamicsProcessingSessionId = secondaryDynamicsProcessingSessionId
            secondaryDynamicsProcessing = tempDp
            secondaryDynamicsProcessingSessionId = tempDpId

            co.touchlab.kermit.Logger.d { "startCrossfade FINISHED successfully" }

            // Re-apply equalizer settings to the new active player's session
            val finalSettingsViewModel: SettingsViewModel by inject()
            updateEqualizer(finalSettingsViewModel.settingsState.value.isEqEnabled, finalSettingsViewModel.settingsState.value.eqBands)
            updateDynamicsProcessing(finalSettingsViewModel.settingsState.value.isVolumeNormalizationEnabled)

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

    private fun updateEqualizer(enabled: Boolean, bands: List<Float>, player: ExoPlayer? = currentPlayer) {
        try {
            val pl = player ?: return
            val sessionId = pl.audioSessionId
            if (sessionId == androidx.media3.common.C.AUDIO_SESSION_ID_UNSET) {
                if (pl == currentPlayer) {
                    equalizer?.release()
                    equalizer = null
                } else {
                    secondaryEqualizer?.release()
                    secondaryEqualizer = null
                }
                return
            }

            val isPrimary = (pl == currentPlayer)
            var eq = if (isPrimary) equalizer else secondaryEqualizer
            val storedSessionId = if (isPrimary) equalizerSessionId else secondaryEqualizerSessionId

            if (eq == null || storedSessionId != sessionId) {
                eq?.release()
                eq = android.media.audiofx.Equalizer(0, sessionId)
                if (isPrimary) {
                    equalizer = eq
                    equalizerSessionId = sessionId
                } else {
                    secondaryEqualizer = eq
                    secondaryEqualizerSessionId = sessionId
                }
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

    private fun updateDynamicsProcessing(enabled: Boolean, player: ExoPlayer? = currentPlayer) {
        try {
            val pl = player ?: return
            val sessionId = pl.audioSessionId
            if (sessionId == androidx.media3.common.C.AUDIO_SESSION_ID_UNSET) {
                if (pl == currentPlayer) {
                    dynamicsProcessing?.release()
                    dynamicsProcessing = null
                } else {
                    secondaryDynamicsProcessing?.release()
                    secondaryDynamicsProcessing = null
                }
                return
            }

            val isPrimary = (pl == currentPlayer)
            var dp = if (isPrimary) dynamicsProcessing else secondaryDynamicsProcessing
            val storedSessionId = if (isPrimary) dynamicsProcessingSessionId else secondaryDynamicsProcessingSessionId

            if (dp == null || storedSessionId != sessionId) {
                dp?.release()
                val builder = android.media.audiofx.DynamicsProcessing.Config.Builder(
                    0, 1, false, 0, false, 0, false, 0, true
                )
                dp = android.media.audiofx.DynamicsProcessing(0, sessionId, builder.build())
                if (isPrimary) {
                    dynamicsProcessing = dp
                    dynamicsProcessingSessionId = sessionId
                } else {
                    secondaryDynamicsProcessing = dp
                    secondaryDynamicsProcessingSessionId = sessionId
                }
            }

            if (enabled) {
                val limiter = android.media.audiofx.DynamicsProcessing.Limiter(
                    true, true, 0, 1.0f, 50.0f, 1.0f, -12.0f, 0.0f
                )
                dp.setLimiterAllChannelsTo(limiter)
            }
            dp.enabled = enabled
        } catch (e: Exception) {
            android.util.Log.e("MusicService", "Error updating DynamicsProcessing: ${e.message}", e)
        }
    }

    override fun onDestroy() {
        progressMonitorJob?.cancel()
        crossfadeJob?.cancel()
        mediaSession?.run {
            release()
            mediaSession = null
        }
        abandonAudioFocus()
        equalizer?.release()
        equalizer = null
        secondaryEqualizer?.release()
        secondaryEqualizer = null
        dynamicsProcessing?.release()
        dynamicsProcessing = null
        secondaryDynamicsProcessing?.release()
        secondaryDynamicsProcessing = null
        exoPlayer?.release()
        exoPlayer = null
        secondaryExoPlayer?.release()
        secondaryExoPlayer = null
        currentPlayer = null
        serviceScope.cancel()
        super.onDestroy()
    }
}
