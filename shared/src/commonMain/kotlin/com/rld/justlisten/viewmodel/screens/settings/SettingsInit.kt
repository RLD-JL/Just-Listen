package com.rld.justlisten.viewmodel.screens.settings

import com.rld.justlisten.Navigation
import com.rld.justlisten.datalayer.datacalls.settings.getSettingsInfo
import com.rld.justlisten.viewmodel.screens.ScreenInitSettings

fun Navigation.initSettings() = ScreenInitSettings(
    title = "Settings",
    initState = { SettingsState(isLoading = true) },
    callOnInit = {
        val settingsInfo = dataRepository.getSettingsInfo()
        stateManager.updateScreen(SettingsState::class) {
            it.copy(
                isLoading = false,
                hasDonationNavigationOn = settingsInfo.hasNavigationDonationOn,
                isDarkThemeOn = settingsInfo.isDarkThemeOn,
                palletColor = settingsInfo.palletColor
            )
        }
    },
    reinitOnEachNavigation = true
)