package com.rld.justlisten.viewmodel.settings

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.datacalls.settings.getSettingsInfo
import com.rld.justlisten.datalayer.datacalls.settings.saveSettingsInfo
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.update
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: Repository,
) : BaseScreenViewModel() {

    private val _settingsState = MutableStateFlow(SettingsScreenState())
    val settingsState: StateFlow<SettingsScreenState> = _settingsState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val info = repository.getSettingsInfo()
            _settingsState.update {
                it.copy(
                    isLoading = false,
                    hasDonationNavigationOn = info.hasNavigationDonationOn,
                    isDarkThemeOn = info.isDarkThemeOn,
                    palletColor = info.palletColor,
                )
            }
        }
    }

    fun onDarkModeToggled(isDarkMode: Boolean) {
        persist { it.copy(isDarkThemeOn = isDarkMode) }
    }

    fun onDonationToggled(hasDonation: Boolean) {
        persist { it.copy(hasDonationNavigationOn = hasDonation) }
    }

    fun onPaletteSelected(palette: String) {
        persist { it.copy(palletColor = palette) }
    }

    private fun persist(transform: (SettingsScreenState) -> SettingsScreenState) {
        val updated = transform(_settingsState.value)
        _settingsState.value = updated
        repository.saveSettingsInfo(
            updated.hasDonationNavigationOn,
            updated.isDarkThemeOn,
            updated.palletColor,
        )
    }
}

data class SettingsScreenState(
    val isLoading: Boolean = true,
    val isDarkThemeOn: Boolean = false,
    val hasDonationNavigationOn: Boolean = false,
    val palletColor: String = "Dark",
)
