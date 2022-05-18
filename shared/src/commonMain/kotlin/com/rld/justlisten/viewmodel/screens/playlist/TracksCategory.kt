package com.rld.justlisten.viewmodel.screens.playlist

enum class TracksCategory(val value: String) {
    ELECTRONIC("Electronic"),
    ROCK("Rock"),
    RAP("Rap"),
    ALTERNATIVE("Alternative")
}

fun getTrackCategory(): List<TracksCategory> {
    return listOf(TracksCategory.ELECTRONIC, TracksCategory.ROCK, TracksCategory.RAP, TracksCategory.ALTERNATIVE)
}