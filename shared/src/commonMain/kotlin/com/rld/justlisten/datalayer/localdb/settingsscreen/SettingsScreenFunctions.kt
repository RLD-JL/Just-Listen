package com.rld.justlisten.datalayer.localdb.settingsscreen

import com.rld.justlisten.LocalDb
import com.rld.justlisten.database.settingsscreen.SettingsInfo


fun LocalDb.saveSettingsInfo(
    hasNavigationSupportOn: Boolean,
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
    settingsScreenQueries.transaction {
        settingsScreenQueries.upsertSettingsInfo(
            hasNavigationSupportOn = hasNavigationSupportOn,
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
}

fun LocalDb.getSettingsInfo(): SettingsInfo {
    return settingsScreenQueries.getSettingsInfo().executeAsOne()
}

fun LocalDb.blockUser(userId: String, username: String) {
    settingsScreenQueries.transaction {
        settingsScreenQueries.insertBlockedUser(userId, username)
    }
}

fun LocalDb.unblockUser(userId: String) {
    settingsScreenQueries.transaction {
        settingsScreenQueries.deleteBlockedUser(userId)
    }
}

fun LocalDb.getBlockedUsers(): List<com.rld.justlisten.database.settingsscreen.BlockedUser> {
    return settingsScreenQueries.getBlockedUsers().executeAsList()
}

fun LocalDb.hideComment(commentId: String) {
    settingsScreenQueries.transaction {
        settingsScreenQueries.insertHiddenComment(commentId)
    }
}

fun LocalDb.unhideComment(commentId: String) {
    settingsScreenQueries.transaction {
        settingsScreenQueries.deleteHiddenComment(commentId)
    }
}

fun LocalDb.getHiddenComments(): List<String> {
    return settingsScreenQueries.getHiddenComments().executeAsList()
}