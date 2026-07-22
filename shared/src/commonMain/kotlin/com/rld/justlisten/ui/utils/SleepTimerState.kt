package com.rld.justlisten.ui.utils

/**
 * Clock-based state for the sleep timer. Platform services own the actual
 * scheduling mechanism; this class keeps deadline calculations testable and
 * preserves sub-minute time when a running timer is extended.
 */
internal class SleepTimerState(
    private val currentTimeMs: () -> Long,
) {
    private var endTimeMs: Long = 0L

    fun start(minutes: Int): Long {
        require(minutes > 0) { "Sleep timer duration must be positive" }
        val delayMs = minutes.toLong() * MILLIS_PER_MINUTE
        endTimeMs = currentTimeMs() + delayMs
        return delayMs
    }

    fun remainingTimeMs(): Long =
        (endTimeMs - currentTimeMs()).coerceAtLeast(0L)

    fun extend(minutes: Int): Long {
        require(minutes > 0) { "Sleep timer extension must be positive" }
        val now = currentTimeMs()
        endTimeMs = maxOf(endTimeMs, now) + minutes.toLong() * MILLIS_PER_MINUTE
        return endTimeMs - now
    }

    fun clear() {
        endTimeMs = 0L
    }

    private companion object {
        const val MILLIS_PER_MINUTE = 60_000L
    }
}
