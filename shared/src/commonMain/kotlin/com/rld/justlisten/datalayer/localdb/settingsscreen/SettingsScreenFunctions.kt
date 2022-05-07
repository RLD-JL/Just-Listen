package com.rld.justlisten.datalayer.localdb.settingsscreen

import myLocal.db.LocalDb

fun LocalDb.saveSettingsInfo(hasNavigationDonationOn: Boolean, isDarkThemeOn: Boolean, palletColor: String) {
    settingsScreenQueries.transaction {
        settingsScreenQueries.upsertSettingsInfo(hasNavigationDonationOn, isDarkThemeOn, palletColor)
    }
}

fun LocalDb.getSettingsInfo(): SettingsInfo {
    return settingsScreenQueries.getSettingsInfo().executeAsOne()
}