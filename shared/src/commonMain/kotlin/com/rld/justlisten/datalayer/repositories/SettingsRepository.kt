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
        isOngoingStreamEnabled: Boolean,
        isEqEnabled: Boolean = false,
        eqPreset: String = "Flat",
        eqBands: String = ""
    )
    fun getSettingsInfo(): SettingsInfo
    var isCrossfadeEnabled: Boolean
    var crossfadeDurationSeconds: Double
    var crossfadeStyle: String
}

class SettingsRepositoryImpl(
    private val localDb: LocalDb
) : SettingsRepository {

    override var isCrossfadeEnabled: Boolean = false
    override var crossfadeDurationSeconds: Double = 5.0
    override var crossfadeStyle: String = "Radio Segue"

    override fun saveSettingsInfo(
        hasNavigationDonationOn: Boolean,
        isDarkThemeOn: Boolean,
        palletColor: String,
        customPrimary: String?,
        customSecondary: String?,
        customBackground: String?,
        customSurface: String?,
        isFirstLaunch: Boolean,
        isOngoingStreamEnabled: Boolean,
        isEqEnabled: Boolean,
        eqPreset: String,
        eqBands: String
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
            isOngoingStreamEnabled = isOngoingStreamEnabled,
            isEqEnabled = isEqEnabled,
            eqPreset = eqPreset,
            eqBands = eqBands
        )
    }

    override fun getSettingsInfo(): SettingsInfo {
        return localDb.getSettingsInfo()
    }
}
