package com.rld.justlisten

import com.rld.justlisten.util.delay
import com.rld.justlisten.util.formatCountdown
import com.rld.justlisten.util.getNormalizedMaxMinutes
import kotlin.test.Test
import kotlin.test.assertEquals

class SleeperTest {

    @Test
    fun `test 2 hour behind and minutes behind`() {
        val currentHour = 23
        val selectedHour = 21

        val currentMinute = 30
        val selectedMinute = 10

        val delay = delay(currentHour, selectedHour, currentMinute, selectedMinute)
        val expectedDelay = (24 - 23 + 21) * 60 + 40
        assertEquals(expectedDelay.toLong(), delay)
    }

    @Test
    fun `test 10 minutes difference`() {
        val currentHour = 23
        val selectedHour = 23

        val currentMinute = 30
        val selectedMinute = 40
        val delay = delay(currentHour, selectedHour, currentMinute, selectedMinute)
        assertEquals(10L, delay)
    }

    @Test
    fun `test 2 hours and 10 minutes difference`() {
        val currentHour = 14
        val selectedHour = 16

        val currentMinute = 30
        val selectedMinute = 40
        val delay = delay(currentHour, selectedHour, currentMinute, selectedMinute)
        assertEquals(130L, delay)
    }

    @Test
    fun `test formatCountdown formats seconds correctly`() {
        // Less than 1 minute
        assertEquals("45s", formatCountdown(45 * 1000L))
        assertEquals("0s", formatCountdown(0L))
    }

    @Test
    fun `test formatCountdown formats minutes and seconds correctly`() {
        // Minutes and seconds
        assertEquals("2m 5s", formatCountdown(125 * 1000L))
        assertEquals("59m 59s", formatCountdown((59 * 60 + 59) * 1000L))
    }

    @Test
    fun `test formatCountdown formats hours minutes and seconds correctly`() {
        // Hours, minutes, and seconds
        assertEquals("1h 1m 5s", formatCountdown(3665 * 1000L))
        assertEquals("2h 0m 0s", formatCountdown(7200 * 1000L))
    }

    @Test
    fun `test getNormalizedMaxMinutes returns 120 for standard ranges`() {
        // Equal to or lower than 120
        assertEquals(120, getNormalizedMaxMinutes(5))
        assertEquals(120, getNormalizedMaxMinutes(30))
        assertEquals(120, getNormalizedMaxMinutes(120))
    }

    @Test
    fun `test getNormalizedMaxMinutes returns dynamic scale for custom large ranges`() {
        // Larger than 120
        assertEquals(180, getNormalizedMaxMinutes(180))
        assertEquals(240, getNormalizedMaxMinutes(240))
        assertEquals(1000, getNormalizedMaxMinutes(1000))
    }
}