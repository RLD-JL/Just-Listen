package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.LocalDb
import com.rld.justlisten.database.settingsscreen.SettingsInfo
import com.rld.justlisten.datalayer.localdb.settingsscreen.getSettingsInfo
import com.rld.justlisten.datalayer.localdb.settingsscreen.saveSettingsInfo

interface SettingsRepository {
    fun saveSettingsInfo(
        hasNavigationDonationOn: Boolean,
        isDarkThemeOn: Boolean,
        palletColor: String,
        customPrimary: String?,
        customSecondary: String?,
        customBackground: String?,
        customSurface: String?,
        isFirstLaunch: Boolean,
        isOngoingStreamEnabled: Boolean
    )
    fun getSettingsInfo(): SettingsInfo
}

class SettingsRepositoryImpl(
    private val localDb: LocalDb
) : SettingsRepository {

    override fun saveSettingsInfo(
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
        localDb.saveSettingsInfo(
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

    override fun getSettingsInfo(): SettingsInfo {
        return localDb.getSettingsInfo()
    }
}
