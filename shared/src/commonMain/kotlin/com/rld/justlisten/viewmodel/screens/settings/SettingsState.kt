package com.rld.justlisten.viewmodel.screens.settings

import com.rld.justlisten.ScreenState

data class SettingsState(
    val isLoading: Boolean = false,
    val hasDonationNavigationOn: Boolean = true,
    val isDarkThemeOn: Boolean = true,
    val palletColor: String = "Pink",
    val customPrimary: String? = null,
    val customSecondary: String? = null,
    val customBackground: String? = null,
    val customSurface: String? = null,
) : ScreenState