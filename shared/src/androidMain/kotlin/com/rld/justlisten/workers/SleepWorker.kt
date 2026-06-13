package com.rld.justlisten.workers

import android.content.Context
import com.rld.justlisten.media.exoplayer.MusicServiceConnection
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.system.exitProcess

class SleepWorker(val context: Context, parameters: WorkerParameters) : 
    CoroutineWorker(context, parameters), KoinComponent {

    private val musicServiceConnection: MusicServiceConnection by inject()

    override suspend fun doWork(): Result {
        val sharedPrefs = context.getSharedPreferences("sleep_timer_prefs", Context.MODE_PRIVATE)
        val fadeOutEnabled = sharedPrefs.getBoolean("sleep_timer_fade_out", true)

        if (fadeOutEnabled) {
            // Volume changes on ExoPlayer must occur on the Main dispatcher
            withContext(Dispatchers.Main) {
                runCatching {
                    val controller = musicServiceConnection.mediaController
                    if (controller != null && controller.isPlaying) {
                        val startVolume = controller.volume
                        val steps = 10
                        val delayStepMs = 1500L // 15 seconds total fade out duration
                        for (i in steps downTo 0) {
                            controller.volume = startVolume * (i.toFloat() / steps)
                            kotlinx.coroutines.delay(delayStepMs)
                        }
                    }
                }
            }
        }

        // Clean up preference state so the app knows the timer finished
        sharedPrefs.edit()
            .putLong("sleep_timer_end_time_ms", 0L)
            .apply()

        // Cleanly stop the player and let OS manage process lifecycle
        withContext(Dispatchers.Main) {
            runCatching {
                musicServiceConnection.mediaController?.stop()
            }
        }
        return Result.success()
    }
}
