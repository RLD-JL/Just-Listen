package com.rld.justlisten.viewmodel.settings

import com.rld.justlisten.datalayer.repositories.SettingsRepository
import com.rld.justlisten.datalayer.repositories.AuthRepository
import com.rld.justlisten.datalayer.repositories.SyncRepository
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserFavoriteTracks
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserPlaylists
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserFavoritePlaylists
import com.rld.justlisten.datalayer.repositories.SessionState
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.settings.SettingsState
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val apiClient: ApiClient,
) : BaseScreenViewModel() {
    
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadSettings()

        // Check/refresh existing session on launch
        viewModelScope.launch {
            authRepository.refreshSession()
        }

        // Collect session state
        viewModelScope.launch {
            authRepository.sessionState.collect { session ->
                _settingsState.value = _settingsState.value.copy(sessionState = session)
            }
        }

        // Collect sync state
        viewModelScope.launch {
            syncRepository.syncState.collect { sync ->
                _settingsState.value = _settingsState.value.copy(syncState = sync)
            }
        }
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                val saved = withContext(Dispatchers.IO) {
                    settingsRepository.getSettingsInfo()
                }
                _settingsState.value = _settingsState.value.copy(
                    hasDonationNavigationOn = saved.hasNavigationDonationOn,
                    isDarkThemeOn = saved.isDarkThemeOn,
                    palletColor = saved.palletColor,
                    customPrimary = saved.customPrimary,
                    customSecondary = saved.customSecondary,
                    customBackground = saved.customBackground,
                    customSurface = saved.customSurface,
                    isFirstLaunch = saved.isFirstLaunch,
                    isOngoingStreamEnabled = saved.isOngoingStreamEnabled
                )
            } catch (_: Exception) {
                // First run — use defaults
            }
        }
    }

    private fun persistSettings() {
        val state = _settingsState.value
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.saveSettingsInfo(
                hasNavigationDonationOn = state.hasDonationNavigationOn,
                isDarkThemeOn = state.isDarkThemeOn,
                palletColor = state.palletColor,
                customPrimary = state.customPrimary,
                customSecondary = state.customSecondary,
                customBackground = state.customBackground,
                customSurface = state.customSurface,
                isFirstLaunch = state.isFirstLaunch,
                isOngoingStreamEnabled = state.isOngoingStreamEnabled
            )
        }
    }

    fun onOngoingStreamToggled(enabled: Boolean) {
        _settingsState.value = _settingsState.value.copy(isOngoingStreamEnabled = enabled)
        persistSettings()
    }

    fun getAuthUrl(redirectUri: String): String {
        return authRepository.getAuthUrl(redirectUri)
    }

    fun loginWithCode(code: String, redirectUri: String) {
        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(isLoading = true)
            val success = authRepository.loginWithCode(code, redirectUri)
            _settingsState.value = _settingsState.value.copy(isLoading = false)
            if (success) {
                completeOnboarding()
            }
        }
    }

    fun logout() {
        authRepository.logout()
        syncRepository.clearQueue()
    }

    fun completeOnboarding() {
        _settingsState.value = _settingsState.value.copy(isFirstLaunch = false)
        persistSettings()
    }

    fun retryFailedSync() {
        syncRepository.triggerSync()
    }

    fun clearFailedSync() {
        syncRepository.clearQueue()
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
