package com.rld.justlisten.viewmodel.settings

import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.datacalls.settings.getSettingsInfo
import com.rld.justlisten.datalayer.datacalls.settings.saveSettingsInfo
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.settings.SettingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val repository: Repository,
) : BaseScreenViewModel() {
    
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        try {
            val saved = repository.getSettingsInfo()
            _settingsState.value = SettingsState(
                hasDonationNavigationOn = saved.hasNavigationDonationOn,
                isDarkThemeOn = saved.isDarkThemeOn,
                palletColor = saved.palletColor,
            )
        } catch (_: Exception) {
            // First run — use defaults
        }
    }

    private fun persistSettings() {
        val state = _settingsState.value
        repository.saveSettingsInfo(
            state.hasDonationNavigationOn,
            state.isDarkThemeOn,
            state.palletColor
        )
    }

    fun onDarkModeToggled(isDarkThemeOn: Boolean) {
        _settingsState.value = _settingsState.value.copy(isDarkThemeOn = isDarkThemeOn)
        persistSettings()
    }
    
    fun onDonationToggled(hasDonationNavigationOn: Boolean) {
        _settingsState.value = _settingsState.value.copy(hasDonationNavigationOn = hasDonationNavigationOn)
        persistSettings()
    }

    fun onPaletteSelected(color: String) {
        _settingsState.value = _settingsState.value.copy(palletColor = color)
        persistSettings()
    }
}
