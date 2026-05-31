package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.LocalDb
import com.rld.justlisten.database.settingsscreen.SettingsInfo
import com.rld.justlisten.datalayer.localdb.settingsscreen.getSettingsInfo
import com.rld.justlisten.datalayer.localdb.settingsscreen.saveSettingsInfo

interface SettingsRepository {
    fun saveSettingsInfo(hasNavigationDonationOn: Boolean, isDarkThemeOn: Boolean, palletColor: String)
    fun getSettingsInfo(): SettingsInfo
}

class SettingsRepositoryImpl(
    private val localDb: LocalDb
) : SettingsRepository {

    override fun saveSettingsInfo(
        hasNavigationDonationOn: Boolean,
        isDarkThemeOn: Boolean,
        palletColor: String
    ) {
        localDb.saveSettingsInfo(hasNavigationDonationOn, isDarkThemeOn, palletColor)
    }

    override fun getSettingsInfo(): SettingsInfo {
        return localDb.getSettingsInfo()
    }
}
