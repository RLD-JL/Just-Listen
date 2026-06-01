package com.rld.justlisten.datalayer.datacalls.settings

import com.rld.justlisten.database.settingsscreen.SettingsInfo
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.localdb.settingsscreen.getSettingsInfo
import com.rld.justlisten.datalayer.localdb.settingsscreen.saveSettingsInfo

fun Repository.saveSettingsInfo(
    hasNavigationDonationOn: Boolean,
    isDarkThemeOn: Boolean,
    palletColor: String,
    customPrimary: String? = null,
    customSecondary: String? = null,
    customBackground: String? = null,
    customSurface: String? = null
) {
    localDb.saveSettingsInfo(
        hasNavigationDonationOn = hasNavigationDonationOn,
        isDarkThemeOn = isDarkThemeOn,
        palletColor = palletColor,
        customPrimary = customPrimary,
        customSecondary = customSecondary,
        customBackground = customBackground,
        customSurface = customSurface
    )
}

fun Repository.getSettingsInfo() : SettingsInfo {
    return localDb.getSettingsInfo()
}