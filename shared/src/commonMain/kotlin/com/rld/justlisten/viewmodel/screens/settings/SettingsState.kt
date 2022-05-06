package com.rld.justlisten.viewmodel.screens.settings

import com.rld.justlisten.ScreenState

data class SettingsState(
    val isLoading: Boolean = false,
    val hasFundNavigationOn: Boolean = true,
    val isDarkThemeOn: Boolean = true,
    val palletColor: String = "Dark"
) : ScreenState