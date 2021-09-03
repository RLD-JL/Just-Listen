package com.example.audius.viewmodel.screens

import com.example.audius.Navigation
import com.example.audius.ScreenIdentifier
import com.example.audius.viewmodel.screens.trending.initPlaylist
import com.example.audius.viewmodel.screens.trending.initTrendingList

enum class Screen (
    val asString: String,
    val navigationLevel : Int = 1,
    val initSettings: Navigation.(ScreenIdentifier) -> ScreenInitSettings,
    val stackableInstances : Boolean = false,
) {
    TrendingList("trendingList", 1, {initTrendingList(it.params())}),
    Playlist("playlist", 1, {initPlaylist(it.params())})
}