package com.example.audius.viewmodel.screens

import com.example.audius.ScreenIdentifier
import com.example.audius.viewmodel.screens.trending.TrendingListParams

object navigationSettings {
    val homeScreen = Level1Navigation.AllTrending // the start screen should be specified here
    val saveLastLevel1Screen = true
    val alwaysQuitOnHomeScreen = true
}


// LEVEL 1 NAVIGATION OF THE APP

enum class Level1Navigation(val screenIdentifier: ScreenIdentifier, val rememberVerticalStack: Boolean = false) {
    AllTrending( ScreenIdentifier.get(Screen.TrendingList, TrendingListParams("")), true)
}