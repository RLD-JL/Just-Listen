package com.rld.justlisten.viewmodel.settings

import com.rld.justlisten.datalayer.Repository
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
    
    fun onDarkModeToggled(isDarkThemeOn: Boolean) {
        _settingsState.value = _settingsState.value.copy(isDarkThemeOn = isDarkThemeOn)
    }
    
    fun onDonationToggled(hasDonationNavigationOn: Boolean) {
        _settingsState.value = _settingsState.value.copy(hasDonationNavigationOn = hasDonationNavigationOn)
    }

    fun onPaletteSelected(color: String) {
        _settingsState.value = _settingsState.value.copy(palletColor = color)
    }
}
