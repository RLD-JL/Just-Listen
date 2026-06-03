package com.rld.justlisten.ui.utils

import kotlinx.coroutines.*
import org.koin.mp.KoinPlatform
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.IOSMusicPlayer

class IosSleepTimerService : SleepTimerService {
    private var endTimeMs: Long = 0L
    private var fadeOutOption: Boolean = true
    private var sleepJob: Job? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val musicPlayer: MusicPlayer by lazy {
        KoinPlatform.getKoin().get()
    }

    override fun getRemainingTimeMs(): Long {
        val now = getCurrentTimeMs()
        return if (endTimeMs > now) endTimeMs - now else 0L
    }

    override fun setTimer(minutes: Int, fadeOut: Boolean) {
        sleepJob?.cancel()
        fadeOutOption = fadeOut
        
        val delayMs = minutes * 60 * 1000L
        endTimeMs = getCurrentTimeMs() + delayMs

        sleepJob = scope.launch {
            delay(delayMs)
            
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
            
            cancelTimer()
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
        sleepJob?.cancel()
        sleepJob = null
        endTimeMs = 0L
    }
}
