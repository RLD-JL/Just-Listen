package com.rld.justlisten.ui.utils

import java.util.Calendar

actual fun getCurrentTimeMs(): Long {
    return System.currentTimeMillis()
}

actual fun getGreetingText(): String {
    val rightNow = Calendar.getInstance()
    return when (rightNow.get(Calendar.HOUR_OF_DAY)) {
        in 0..5 -> "Chilling"
        in 6..11 -> "Good Morning"
        in 12..17 -> "Hey there"
        in 18..23 -> "Good Evening"
        else -> "Hello"
    }
}

actual fun getStopTimeText(minutes: Int): String {
    val rightNow = Calendar.getInstance()
    rightNow.add(Calendar.MINUTE, minutes)
    val hour = rightNow.get(Calendar.HOUR_OF_DAY)
    val minute = rightNow.get(Calendar.MINUTE)
    val ampm = if (hour >= 12) "PM" else "AM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val displayMinute = if (minute < 10) "0$minute" else "$minute"
    return "Stops at $displayHour:$displayMinute $ampm"
}
