package com.rld.justlisten.workers

import android.content.Context
import com.rld.justlisten.media.exoplayer.MusicServiceConnection
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rld.justlisten.ui.utils.createSleepTimerFadePlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SleepWorker(val context: Context, parameters: WorkerParameters) : 
    CoroutineWorker(context, parameters), KoinComponent {

    private val musicServiceConnection: MusicServiceConnection by inject()

    override suspend fun doWork(): Result {
        withContext(Dispatchers.Main) {
            musicServiceConnection.awaitController()
        }
        val sharedPrefs = context.getSharedPreferences("sleep_timer_prefs", Context.MODE_PRIVATE)
        val fadeOutEnabled = sharedPrefs.getBoolean("sleep_timer_fade_out", true)

        var originalVolume = 1.0f
        withContext(Dispatchers.Main) {
            runCatching {
                val controller = musicServiceConnection.mediaController
                if (controller != null) {
                    originalVolume = controller.volume
                }
            }
        }

        if (fadeOutEnabled) {
            val fadePlan = createSleepTimerFadePlan(originalVolume)
            // Volume changes on ExoPlayer must occur on the Main dispatcher
            withContext(Dispatchers.Main) {
                runCatching {
                    val controller = musicServiceConnection.mediaController
                    if (controller != null && controller.isPlaying) {
                        val delayStepMs = 1500L // 15 seconds total fade out duration
                        fadePlan.rampDownVolumes.forEachIndexed { index, volume ->
                            controller.volume = volume
                            if (index < fadePlan.rampDownVolumes.lastIndex) {
                                kotlinx.coroutines.delay(delayStepMs)
                            }
                        }
                    }
                }
            }
        }

        // Clean up preference state so the app knows the timer finished
        sharedPrefs.edit()
            .putLong("sleep_timer_end_time_ms", 0L)
            .apply()

        // Pause playback without stopping the service or terminating the app.
        withContext(Dispatchers.Main) {
            runCatching {
                val controller = musicServiceConnection.mediaController
                controller?.pause()
                controller?.volume = createSleepTimerFadePlan(originalVolume).restoreVolume
            }
        }
        return Result.success()
    }
}
