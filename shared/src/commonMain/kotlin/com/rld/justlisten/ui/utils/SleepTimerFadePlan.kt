package com.rld.justlisten.ui.utils

internal data class SleepTimerFadePlan(
    val rampDownVolumes: List<Float>,
    val restoreVolume: Float,
)

internal fun createSleepTimerFadePlan(
    originalVolume: Float,
    steps: Int = 10,
): SleepTimerFadePlan {
    require(originalVolume >= 0f) { "Original volume must not be negative" }
    require(steps > 0) { "Fade steps must be positive" }

    return SleepTimerFadePlan(
        rampDownVolumes = (steps downTo 0).map { step ->
            originalVolume * (step.toFloat() / steps)
        },
        restoreVolume = originalVolume,
    )
}
