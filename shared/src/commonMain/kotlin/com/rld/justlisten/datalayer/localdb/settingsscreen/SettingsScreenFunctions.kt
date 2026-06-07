package com.rld.justlisten.datalayer.localdb.settingsscreen

import com.rld.justlisten.LocalDb
import com.rld.justlisten.database.settingsscreen.SettingsInfo


fun LocalDb.saveSettingsInfo(
    hasNavigationDonationOn: Boolean,
    isDarkThemeOn: Boolean,
    palletColor: String,
    customPrimary: String?,
    customSecondary: String?,
    customBackground: String?,
    customSurface: String?,
    isFirstLaunch: Boolean,
    isOngoingStreamEnabled: Boolean
) {
    settingsScreenQueries.transaction {
        settingsScreenQueries.upsertSettingsInfo(
            hasNavigationDonationOn = hasNavigationDonationOn,
            isDarkThemeOn = isDarkThemeOn,
            palletColor = palletColor,
            customPrimary = customPrimary,
            customSecondary = customSecondary,
            customBackground = customBackground,
            customSurface = customSurface,
            isFirstLaunch = isFirstLaunch,
            isOngoingStreamEnabled = isOngoingStreamEnabled
        )
    }
}

fun LocalDb.getSettingsInfo(): SettingsInfo {
    return settingsScreenQueries.getSettingsInfo().executeAsOne()
}