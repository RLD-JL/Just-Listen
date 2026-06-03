@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package com.rld.justlisten.ui.utils

import platform.Foundation.NSDate
import platform.Foundation.NSCalendar
import platform.Foundation.NSCalendarUnitHour
import platform.Foundation.NSCalendarUnitMinute
import platform.Foundation.timeIntervalSince1970

actual fun getCurrentTimeMs(): Long {
    return (NSDate().timeIntervalSince1970 * 1000).toLong()
}

actual fun getGreetingText(): String {
    val calendar = NSCalendar.currentCalendar
    val date = NSDate()
    val hour = calendar.component(NSCalendarUnitHour, fromDate = date).toInt()
    return when (hour) {
        in 0..5 -> "Chilling"
        in 6..11 -> "Good Morning"
        in 12..17 -> "Hey there"
        in 18..23 -> "Good Evening"
        else -> "Hello"
    }
}

actual fun getStopTimeText(minutes: Int): String {
    val calendar = NSCalendar.currentCalendar
    val stopDate = calendar.dateByAddingUnit(
        unit = NSCalendarUnitMinute,
        value = minutes.toLong(),
        toDate = NSDate(),
        options = 0UL
    ) ?: NSDate()
    val hour = calendar.component(NSCalendarUnitHour, fromDate = stopDate).toInt()
    val minute = calendar.component(NSCalendarUnitMinute, fromDate = stopDate).toInt()
    val ampm = if (hour >= 12) "PM" else "AM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    val displayMinute = if (minute < 10) "0$minute" else "$minute"
    return "Stops at $displayHour:$displayMinute $ampm"
}
