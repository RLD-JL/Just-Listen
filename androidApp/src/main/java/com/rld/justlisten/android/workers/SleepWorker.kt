package com.rld.justlisten.android.workers

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.system.exitProcess

class SleepWorker(val context: Context, parameters: WorkerParameters) : 
    CoroutineWorker(context, parameters), KoinComponent {

    private val exoPlayer: ExoPlayer by inject()

    override suspend fun doWork(): Result {
        val sharedPrefs = context.getSharedPreferences("sleep_timer_prefs", Context.MODE_PRIVATE)
        val fadeOutEnabled = sharedPrefs.getBoolean("sleep_timer_fade_out", true)

        if (fadeOutEnabled) {
            withContext(Dispatchers.Main) {
                runCatching {
                    if (exoPlayer.isPlaying) {
                        val startVolume = exoPlayer.volume
                        val steps = 10
                        val delayStepMs = 1500L
                        for (i in steps downTo 0) {
                            exoPlayer.volume = startVolume * (i.toFloat() / steps)
                            kotlinx.coroutines.delay(delayStepMs)
                        }
                    }
                }
            }
        }

        sharedPrefs.edit()
            .putLong("sleep_timer_end_time_ms", 0L)
            .apply()

        exitProcess(0)
    }
}
