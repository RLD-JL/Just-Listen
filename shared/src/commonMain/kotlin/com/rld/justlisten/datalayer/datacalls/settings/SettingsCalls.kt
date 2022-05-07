package com.rld.justlisten.datalayer.datacalls.settings

import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.localdb.settingsscreen.SettingsInfo
import com.rld.justlisten.datalayer.localdb.settingsscreen.getSettingsInfo
import com.rld.justlisten.datalayer.localdb.settingsscreen.saveSettingsInfo

fun Repository.saveSettingsInfo(hasNavigationDonationOn: Boolean, isDarkThemeOn: Boolean, palletColor: String) {
    localDb.saveSettingsInfo(hasNavigationDonationOn, isDarkThemeOn, palletColor)
}

fun Repository.getSettingsInfo() : SettingsInfo {
    return localDb.getSettingsInfo()
}