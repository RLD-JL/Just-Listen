package com.rld.justlisten

import com.rld.justlisten.util.delay
import kotlin.test.Test
import kotlin.test.assertEquals

class SleeperTest {

    @Test
    fun `test 2 hour behind and minutes  behind`() {
        val currentHour = 23
        val selectedHour = 21

        val currentMinute = 30
        val selectedMinute = 10

        val delay = delay(currentHour, selectedHour, currentMinute, selectedMinute)
        val expectedDelay = (24 - 23 + 21) * 60 +  40
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
}