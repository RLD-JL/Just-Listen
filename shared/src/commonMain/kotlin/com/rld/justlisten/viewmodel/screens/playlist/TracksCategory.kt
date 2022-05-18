package com.rld.justlisten.viewmodel.screens.playlist

enum class TracksCategory(val value: String) {
    ELECTRONIC("Electronic"),
    ROCK("Rock"),
    RAP("Rap"),
    ALTERNATIVE("Alternative")
}

enum class TimeRange(val value: String) {
    WEEK("Week"),
    MONTH("Month"),
    ALLTIME("All Time")
}

fun getTrackCategory(): List<TracksCategory> {
    return listOf(TracksCategory.ELECTRONIC, TracksCategory.ROCK, TracksCategory.RAP, TracksCategory.ALTERNATIVE)
}

fun getTimeRange(): List<TimeRange> {
    return listOf(TimeRange.WEEK,TimeRange.MONTH,TimeRange.ALLTIME)
}