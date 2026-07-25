@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package com.rld.justlisten.ui.utils

import kotlinx.coroutines.*
import org.koin.mp.KoinPlatform
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.IOSMusicPlayer
import platform.darwin.*
import platform.UIKit.*

class IosSleepTimerService : SleepTimerService {
    private val timerState = SleepTimerState { getCurrentTimeMs() }
    private var fadeOutOption: Boolean = true
    private var sleepJob: Job? = null
    private var nativeTimer: dispatch_source_t = null
    private var backgroundTaskId: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val musicPlayer: MusicPlayer by lazy {
        KoinPlatform.getKoin().get()
    }

    override fun getRemainingTimeMs(): Long {
        return timerState.remainingTimeMs()
    }

    private fun startBackgroundTask() {
        if (backgroundTaskId == UIBackgroundTaskInvalid) {
            backgroundTaskId = UIApplication.sharedApplication.beginBackgroundTaskWithExpirationHandler {
                endBackgroundTask()
            }
        }
    }

    private fun endBackgroundTask() {
        if (backgroundTaskId != UIBackgroundTaskInvalid) {
            UIApplication.sharedApplication.endBackgroundTask(backgroundTaskId)
            backgroundTaskId = UIBackgroundTaskInvalid
        }
    }

    override fun setTimer(minutes: Int, fadeOut: Boolean) {
        cancelTimer()
        fadeOutOption = fadeOut
        
        val delayMs = timerState.start(minutes)

        scheduleTimer(delayMs)
    }

    private fun scheduleTimer(delayMs: Long) {
        val queue = dispatch_get_main_queue()
        val timer = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0uL, 0uL, queue)
        nativeTimer = timer

        val time = dispatch_time(DISPATCH_TIME_NOW, delayMs * 1_000_000L)
        dispatch_source_set_timer(timer, time, DISPATCH_TIME_FOREVER, 0uL)

        dispatch_source_set_event_handler(timer) {
            executeSleepActions()
        }
        dispatch_resume(timer)
    }

    private fun executeSleepActions() {
        startBackgroundTask()
        sleepJob = scope.launch {
            try {
                val iosPlayer = musicPlayer as? IOSMusicPlayer
                val fadePlan = if (fadeOutOption && iosPlayer != null) {
                    createSleepTimerFadePlan(iosPlayer.volume)
                } else {
                    null
                }
                if (fadePlan != null && iosPlayer != null) {
                    val delayStepMs = 1500L // 15 seconds total fade out
                    fadePlan.rampDownVolumes.forEachIndexed { index, volume ->
                        iosPlayer.volume = volume
                        if (index < fadePlan.rampDownVolumes.lastIndex) {
                            delay(delayStepMs)
                        }
                    }
                }
                
                musicPlayer.pause()
                
                // Restore the exact pre-fade level while playback is paused.
                if (fadePlan != null && iosPlayer != null) {
                    iosPlayer.volume = fadePlan.restoreVolume
                }
            } finally {
                cancelTimer()
                endBackgroundTask()
            }
        }
    }

    override fun extendTimer(minutes: Int) {
        val newDelayMs = timerState.extend(minutes)
        nativeTimer?.let { dispatch_source_cancel(it) }
        nativeTimer = null
        scheduleTimer(newDelayMs)
    }

    override fun cancelTimer() {
        nativeTimer?.let {
            dispatch_source_cancel(it)
        }
        nativeTimer = null
        sleepJob?.cancel()
        sleepJob = null
        timerState.clear()
        endBackgroundTask()
    }
}
