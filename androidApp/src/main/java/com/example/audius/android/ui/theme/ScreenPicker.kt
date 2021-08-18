package com.example.audius.android.ui.theme

import androidx.compose.runtime.Composable
import com.example.audius.Navigation
import com.example.audius.ScreenIdentifier
import com.example.audius.android.ui.TrendingListScreen
import com.example.audius.viewmodel.screens.Screen
import com.example.audius.viewmodel.screens.trending.playMusic

@Composable
fun Navigation.ScreenPicker(
    screenIdentifier: ScreenIdentifier
) {

    when (screenIdentifier.screen) {

        Screen.TrendingList ->
            TrendingListScreen(
                trendingListState = stateProvider.get(screenIdentifier),
                onLastItemClick = { events.playMusic(it)
                },
            )
    }
    }
