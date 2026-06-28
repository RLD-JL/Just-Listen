package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.LocalDb
import com.rld.justlisten.database.settingsscreen.SettingsInfo
import com.rld.justlisten.datalayer.localdb.settingsscreen.*

interface SettingsRepository {
    fun saveSettingsInfo(
        hasNavigationSupportOn: Boolean,
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
    var isVolumeNormalizationEnabled: Boolean

    fun blockUser(userId: String, username: String)
    fun unblockUser(userId: String)
    fun getBlockedUsers(): List<com.rld.justlisten.database.settingsscreen.BlockedUser>
    fun hideComment(commentId: String)
    fun unhideComment(commentId: String)
    fun getHiddenComments(): List<String>
}

class SettingsRepositoryImpl(
    private val localDb: LocalDb
) : SettingsRepository {

    override var isCrossfadeEnabled: Boolean = false
    override var crossfadeDurationSeconds: Double = 5.0
    override var crossfadeStyle: String = "Radio Segue"
    override var isVolumeNormalizationEnabled: Boolean = false

    override fun saveSettingsInfo(
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
        localDb.saveSettingsInfo(
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

    override fun getSettingsInfo(): SettingsInfo {
        return localDb.getSettingsInfo()
    }

    override fun blockUser(userId: String, username: String) {
        localDb.blockUser(userId, username)
    }

    override fun unblockUser(userId: String) {
        localDb.unblockUser(userId)
    }

    override fun getBlockedUsers(): List<com.rld.justlisten.database.settingsscreen.BlockedUser> {
        return localDb.getBlockedUsers()
    }

    override fun hideComment(commentId: String) {
        localDb.hideComment(commentId)
    }

    override fun unhideComment(commentId: String) {
        localDb.unhideComment(commentId)
    }

    override fun getHiddenComments(): List<String> {
        return localDb.getHiddenComments()
    }
}
