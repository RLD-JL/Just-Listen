package com.rld.justlisten.viewmodel.screens.playlist

enum class TracksCategory(val value: String) {
    ELECTRONIC("Electronic"),
    ROCK("Rock"),
    RAP("Rap"),
    ALTERNATIVE("Alternative"),
    EXPERIMENTAL("Experimental"),
    PUNK("Punk"),
    POP("Pop"),
    FOLK("Folk"),
    AMBIENT("Ambient"),
    JAZZ("Jazz"),
    CLASSICAL("Classical"),
    COUNTRY("Country"),
    KIDS("Kids"),
    AUDIOBOOKS("Audiobooks"),
}

enum class TimeRange(val value: String) {
    WEEK("Week"),
    MONTH("Month"),
    ALLTIME("All Time")
}

fun getTrackCategory(): List<TracksCategory> {
    return listOf(
        TracksCategory.ELECTRONIC,
        TracksCategory.ROCK,
        TracksCategory.RAP,
        TracksCategory.ALTERNATIVE,
        TracksCategory.EXPERIMENTAL,
        TracksCategory.PUNK,
        TracksCategory.POP,
        TracksCategory.FOLK,
        TracksCategory.AMBIENT,
        TracksCategory.JAZZ,
        TracksCategory.CLASSICAL,
        TracksCategory.COUNTRY,
        TracksCategory.KIDS,
        TracksCategory.AUDIOBOOKS
    )
}

fun getTimeRange(): List<TimeRange> {
    return listOf(TimeRange.WEEK, TimeRange.MONTH, TimeRange.ALLTIME)
}