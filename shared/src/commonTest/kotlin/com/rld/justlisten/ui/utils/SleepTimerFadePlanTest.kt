package com.rld.justlisten.ui.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class SleepTimerFadePlanTest {
    @Test
    fun fadeStartsAtOriginalReachesSilenceAndRestoresOriginal() {
        val originalVolume = 0.37f

        val plan = createSleepTimerFadePlan(originalVolume)

        assertEquals(originalVolume, plan.rampDownVolumes.first())
        assertEquals(0f, plan.rampDownVolumes.last())
        assertEquals(originalVolume, plan.restoreVolume)
        assertTrue(plan.rampDownVolumes.zipWithNext().all { (current, next) -> next <= current })
    }

    @Test
    fun fadeContainsBothEndpointsAndRequestedNumberOfSteps() {
        val plan = createSleepTimerFadePlan(originalVolume = 0.8f, steps = 4)

        assertEquals(listOf(0.8f, 0.6f, 0.4f, 0.2f, 0f), plan.rampDownVolumes)
    }

    @Test
    fun rejectsInvalidFadeInputs() {
        assertFailsWith<IllegalArgumentException> {
            createSleepTimerFadePlan(originalVolume = -0.1f)
        }
        assertFailsWith<IllegalArgumentException> {
            createSleepTimerFadePlan(originalVolume = 1f, steps = 0)
        }
    }
}
