package com.rld.justlisten.ui.actions

import com.rld.justlisten.viewmodel.screens.settings.SettingsState

/**
 * Actions emitted by the Settings screen.
 */
sealed interface SettingsAction {
    data class UpdateSettings(val settingsState: SettingsState) : SettingsAction
}
