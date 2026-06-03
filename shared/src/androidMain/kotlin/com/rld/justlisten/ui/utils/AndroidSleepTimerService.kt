package com.rld.justlisten.ui.utils

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rld.justlisten.workers.SleepWorker
import java.util.concurrent.TimeUnit

class AndroidSleepTimerService(private val context: Context) : SleepTimerService {
    private val workManager by lazy { WorkManager.getInstance(context) }
    private val sharedPrefs by lazy { context.getSharedPreferences("sleep_timer_prefs", Context.MODE_PRIVATE) }

    override fun getRemainingTimeMs(): Long {
        val endTimeMs = sharedPrefs.getLong("sleep_timer_end_time_ms", 0L)
        val now = System.currentTimeMillis()
        return if (endTimeMs > now) endTimeMs - now else 0L
    }

    override fun setTimer(minutes: Int, fadeOut: Boolean) {
        val delayMs = minutes * 60 * 1000L
        val endTimeMs = System.currentTimeMillis() + delayMs

        sharedPrefs.edit()
            .putLong("sleep_timer_end_time_ms", endTimeMs)
            .putBoolean("sleep_timer_fade_out", fadeOut)
            .putInt("sleep_timer_duration_mins", minutes)
            .apply()

        scheduleSleepWorker(minutes.toLong())
    }

    override fun extendTimer(minutes: Int) {
        val currentEndTimeMs = sharedPrefs.getLong("sleep_timer_end_time_ms", 0L)
        val baseTimeMs = if (currentEndTimeMs > System.currentTimeMillis()) currentEndTimeMs else System.currentTimeMillis()
        val newEndTimeMs = baseTimeMs + minutes * 60 * 1000L

        sharedPrefs.edit()
            .putLong("sleep_timer_end_time_ms", newEndTimeMs)
            .apply()

        val minsLeft = (newEndTimeMs - System.currentTimeMillis()) / (60 * 1000L)
        scheduleSleepWorker(maxOf(1, minsLeft))
    }

    override fun cancelTimer() {
        workManager.cancelUniqueWork("SleepWorker")
        sharedPrefs.edit()
            .putLong("sleep_timer_end_time_ms", 0L)
            .apply()
    }

    private fun scheduleSleepWorker(minutesDelay: Long) {
        val myWorkRequest = OneTimeWorkRequestBuilder<SleepWorker>()
            .setInitialDelay(minutesDelay, TimeUnit.MINUTES)
            .build()
        workManager.beginUniqueWork(
            "SleepWorker",
            ExistingWorkPolicy.REPLACE,
            myWorkRequest
        ).enqueue()
    }
}
