@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package com.rld.justlisten.ui.utils

import kotlinx.coroutines.*
import org.koin.mp.KoinPlatform
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.IOSMusicPlayer
import platform.darwin.*
import platform.UIKit.*

class IosSleepTimerService : SleepTimerService {
    private var endTimeMs: Long = 0L
    private var fadeOutOption: Boolean = true
    private var sleepJob: Job? = null
    private var nativeTimer: dispatch_source_t = null
    private var backgroundTaskId: UIBackgroundTaskIdentifier = UIBackgroundTaskInvalid
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val musicPlayer: MusicPlayer by lazy {
        KoinPlatform.getKoin().get()
    }

    override fun getRemainingTimeMs(): Long {
        val now = getCurrentTimeMs()
        return if (endTimeMs > now) endTimeMs - now else 0L
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
        
        val delayMs = minutes * 60 * 1000L
        endTimeMs = getCurrentTimeMs() + delayMs

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
                if (fadeOutOption && iosPlayer != null) {
                    val startVolume = iosPlayer.volume
                    val steps = 10
                    val delayStepMs = 1500L // 15 seconds total fade out
                    for (i in steps downTo 0) {
                        iosPlayer.volume = startVolume * (i.toFloat() / steps)
                        delay(delayStepMs)
                    }
                }
                
                musicPlayer.pause()
                
                // Restore player volume level to default after pausing
                if (fadeOutOption && iosPlayer != null) {
                    iosPlayer.volume = 1.0f
                }
            } finally {
                cancelTimer()
                endBackgroundTask()
            }
        }
    }

    override fun extendTimer(minutes: Int) {
        val now = getCurrentTimeMs()
        val baseTimeMs = if (endTimeMs > now) endTimeMs else now
        val newEndTimeMs = baseTimeMs + minutes * 60 * 1000L
        val minsLeft = (newEndTimeMs - now) / (60 * 1000L)
        
        setTimer(maxOf(1, minsLeft.toInt()), fadeOutOption)
    }

    override fun cancelTimer() {
        nativeTimer?.let {
            dispatch_source_cancel(it)
        }
        nativeTimer = null
        sleepJob?.cancel()
        sleepJob = null
        endTimeMs = 0L
        endBackgroundTask()
    }
}
