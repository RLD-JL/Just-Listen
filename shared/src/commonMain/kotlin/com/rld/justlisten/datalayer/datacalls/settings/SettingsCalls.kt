package com.rld.justlisten.datalayer.datacalls.settings

import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.localdb.settingsscreen.SettingsInfo
import com.rld.justlisten.datalayer.localdb.settingsscreen.getSettingsInfo
import com.rld.justlisten.datalayer.localdb.settingsscreen.saveSettingsInfo

fun Repository.saveSettingsInfo(hasNavigationFundOn: Boolean, isDarkThemeOn: Boolean, palletColor: String) {
    localDb.saveSettingsInfo(hasNavigationFundOn, isDarkThemeOn, palletColor)
}

fun Repository.getSettingsInfo() : SettingsInfo {
    return localDb.getSettingsInfo()
}