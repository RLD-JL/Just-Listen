package com.rld.justlisten.viewmodel.settings

import com.rld.justlisten.datalayer.repositories.SettingsRepository
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.settings.SettingsState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : BaseScreenViewModel() {
    
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        try {
            val saved = settingsRepository.getSettingsInfo()
            _settingsState.value = SettingsState(
                hasDonationNavigationOn = saved.hasNavigationDonationOn,
                isDarkThemeOn = saved.isDarkThemeOn,
                palletColor = saved.palletColor,
                customPrimary = saved.customPrimary,
                customSecondary = saved.customSecondary,
                customBackground = saved.customBackground,
                customSurface = saved.customSurface,
            )
        } catch (_: Exception) {
            // First run — use defaults
        }
    }

    private fun persistSettings() {
        val state = _settingsState.value
        settingsRepository.saveSettingsInfo(
            hasNavigationDonationOn = state.hasDonationNavigationOn,
            isDarkThemeOn = state.isDarkThemeOn,
            palletColor = state.palletColor,
            customPrimary = state.customPrimary,
            customSecondary = state.customSecondary,
            customBackground = state.customBackground,
            customSurface = state.customSurface,
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

    fun updateCustomColors(primary: String?, secondary: String?, background: String?, surface: String?) {
        _settingsState.value = _settingsState.value.copy(
            customPrimary = primary,
            customSecondary = secondary,
            customBackground = background,
            customSurface = surface,
            palletColor = "Custom"
        )
        persistSettings()
    }
}
