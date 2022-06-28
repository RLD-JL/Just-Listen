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

