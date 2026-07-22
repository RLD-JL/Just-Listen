package com.rld.justlisten.ui.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SleepTimerStateTest {
    private var nowMs = 1_000_000L
    private val state = SleepTimerState { nowMs }

    @Test
    fun startCreatesDeadlineAndCountdown() {
        assertEquals(30 * 60_000L, state.start(minutes = 30))
        assertEquals(30 * 60_000L, state.remainingTimeMs())

        nowMs += 5 * 60_000L

        assertEquals(25 * 60_000L, state.remainingTimeMs())
    }

    @Test
    fun extensionPreservesSubMinuteTime() {
        state.start(minutes = 30)
        nowMs += 5 * 60_000L + 12_345L
        val remainingBeforeExtension = state.remainingTimeMs()

        val newDelayMs = state.extend(minutes = 15)

        assertEquals(remainingBeforeExtension + 15 * 60_000L, newDelayMs)
        assertEquals(newDelayMs, state.remainingTimeMs())
    }

    @Test
    fun extendingExpiredTimerStartsFromNow() {
        state.start(minutes = 5)
        nowMs += 10 * 60_000L

        assertEquals(15 * 60_000L, state.extend(minutes = 15))
        assertEquals(15 * 60_000L, state.remainingTimeMs())
    }

    @Test
    fun expiredOrCancelledTimerHasNoRemainingTime() {
        state.start(minutes = 5)
        nowMs += 5 * 60_000L
        assertEquals(0L, state.remainingTimeMs())

        state.start(minutes = 5)
        state.clear()
        assertEquals(0L, state.remainingTimeMs())
    }

    @Test
    fun rejectsNonPositiveDurations() {
        assertFailsWith<IllegalArgumentException> { state.start(minutes = 0) }
        assertFailsWith<IllegalArgumentException> { state.extend(minutes = -1) }
    }
}
