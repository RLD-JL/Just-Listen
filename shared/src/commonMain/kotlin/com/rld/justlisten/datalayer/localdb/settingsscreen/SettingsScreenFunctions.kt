package com.rld.justlisten.datalayer.localdb.settingsscreen

import myLocal.db.LocalDb

fun LocalDb.saveSettingsInfo(hasNavigationFundOn: Boolean, isDarkThemeOn: Boolean) {
    settingsScreenQueries.transaction {
        settingsScreenQueries.upsertSettingsInfo(hasNavigationFundOn, isDarkThemeOn)
    }
}

fun LocalDb.getSettingsInfo(): SettingsInfo {
    return settingsScreenQueries.getSettingsInfo().executeAsOne()
}