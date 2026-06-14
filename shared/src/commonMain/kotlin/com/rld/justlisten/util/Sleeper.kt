package com.rld.justlisten.util


fun delay(
    currentTimeHour: Int,
    closeTimeHour: Int,
    currentTimeMinute: Int,
    closeTimeMinute: Int
): Long {
    val delayHour =
        if (currentTimeHour > closeTimeHour) {
            24 - currentTimeHour + closeTimeHour
        } else if (currentTimeHour == closeTimeHour && currentTimeMinute > closeTimeMinute) {
            23
        } else {
            closeTimeHour - currentTimeHour
        }
    val delayMinute = if (currentTimeMinute > closeTimeMinute) {
        60 - currentTimeMinute + closeTimeMinute
    } else {
        closeTimeMinute - currentTimeMinute
    }
    return (delayHour * 60 + delayMinute).toLong()
}

fun formatCountdown(remainingTimeMs: Long): String {
    val totalSecs = remainingTimeMs / 1000
    val hours = totalSecs / 3600
    val mins = (totalSecs % 3600) / 60
    val secs = totalSecs % 60
    
    return when {
        hours > 0 -> "${hours}h ${mins}m ${secs}s"
        mins > 0 -> "${mins}m ${secs}s"
        else -> "${secs}s"
    }
}

fun getNormalizedMaxMinutes(selectedMins: Int): Int {
    return if (selectedMins > 120) selectedMins else 120
}

