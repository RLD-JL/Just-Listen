package com.rld.justlisten.ui.utils

interface SleepTimerService {
    fun getRemainingTimeMs(): Long
    fun setTimer(minutes: Int, fadeOut: Boolean)
    fun extendTimer(minutes: Int)
    fun cancelTimer()
}
