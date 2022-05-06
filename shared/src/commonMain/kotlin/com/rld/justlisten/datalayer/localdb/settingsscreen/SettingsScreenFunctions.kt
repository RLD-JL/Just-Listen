package com.rld.justlisten.datalayer.localdb.settingsscreen

import myLocal.db.LocalDb

fun LocalDb.saveSettingsInfo(hasNavigationFundOn: Boolean, isDarkThemeOn: Boolean, palletColor: String) {
    settingsScreenQueries.transaction {
        settingsScreenQueries.upsertSettingsInfo(hasNavigationFundOn, isDarkThemeOn, palletColor)
    }
}

fun LocalDb.getSettingsInfo(): SettingsInfo {
    return settingsScreenQueries.getSettingsInfo().executeAsOne()
}