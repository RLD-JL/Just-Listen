package com.rld.justlisten.viewmodel.screens.settings

import com.rld.justlisten.datalayer.datacalls.settings.getSettingsInfo
import com.rld.justlisten.datalayer.datacalls.settings.saveSettingsInfo
import com.rld.justlisten.datalayer.localdb.settingsscreen.SettingsInfo
import com.rld.justlisten.viewmodel.Events

fun Events.saveSettingsInfo(hasNavigationFundOn: Boolean, isDarkThemeOn: Boolean) {
    dataRepository.saveSettingsInfo(hasNavigationFundOn, isDarkThemeOn)
}

fun Events.getSettingsInfo(): SettingsInfo {
    return dataRepository.getSettingsInfo()
}

fun Events.updateScreen() = screenCoroutine {
    val settingsInfo = dataRepository.getSettingsInfo()
    stateManager.updateScreen(SettingsState::class) {
        it.copy(
            hasFundNavigationOn = settingsInfo.hasNavigationFundOn,
            isDarkThemeOn = settingsInfo.isDarkThemeOn
        )

    }
}