package com.rld.justlisten.ui.utils

class IosSleepTimerService : SleepTimerService {
    private var endTimeMs: Long = 0L

    override fun getRemainingTimeMs(): Long {
        val now = getCurrentTimeMs()
        return if (endTimeMs > now) endTimeMs - now else 0L
    }

    override fun setTimer(minutes: Int, fadeOut: Boolean) {
        val delayMs = minutes * 60 * 1000L
        endTimeMs = getCurrentTimeMs() + delayMs
    }

    override fun extendTimer(minutes: Int) {
        val now = getCurrentTimeMs()
        val baseTimeMs = if (endTimeMs > now) endTimeMs else now
        endTimeMs = baseTimeMs + minutes * 60 * 1000L
    }

    override fun cancelTimer() {
        endTimeMs = 0L
    }
}
