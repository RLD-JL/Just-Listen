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
                val bands = try {
                    saved.eqBands.split(",").map { it.toFloat() }
                } catch (e: Exception) {
                    listOf(0f, 0f, 0f, 0f, 0f)
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
                    isOngoingStreamEnabled = saved.isOngoingStreamEnabled,
                    isEqEnabled = saved.isEqEnabled,
                    eqPreset = saved.eqPreset,
                    eqBands = bands,
                    isCrossfadeEnabled = settingsRepository.isCrossfadeEnabled,
                    crossfadeDurationSeconds = settingsRepository.crossfadeDurationSeconds,
                    crossfadeStyle = settingsRepository.crossfadeStyle,
                    isVolumeNormalizationEnabled = settingsRepository.isVolumeNormalizationEnabled,
                    isSettingsLoaded = true
                )
            } catch (_: Exception) {
                // First run — use defaults
                _settingsState.value = _settingsState.value.copy(
                    isSettingsLoaded = true
                )
            }
        }
    }

    private fun persistSettings() {
        val state = _settingsState.value
        settingsRepository.isCrossfadeEnabled = state.isCrossfadeEnabled
        settingsRepository.crossfadeDurationSeconds = state.crossfadeDurationSeconds
        settingsRepository.crossfadeStyle = state.crossfadeStyle
        settingsRepository.isVolumeNormalizationEnabled = state.isVolumeNormalizationEnabled
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
                isOngoingStreamEnabled = state.isOngoingStreamEnabled,
                isEqEnabled = state.isEqEnabled,
                eqPreset = state.eqPreset,
                eqBands = state.eqBands.joinToString(",")
            )
        }
    }

    fun onEqToggled(enabled: Boolean) {
        _settingsState.value = _settingsState.value.copy(isEqEnabled = enabled)
        persistSettings()
    }

    fun onEqPresetSelected(preset: String) {
        val presets = mapOf(
            "Flat" to listOf(0f, 0f, 0f, 0f, 0f),
            "Bass Booster" to listOf(6f, 4f, 0f, 0f, 0f),
            "Rock" to listOf(4f, 2f, -2f, 2f, 5f),
            "Pop" to listOf(-2f, 1f, 4f, 2f, -2f),
            "Classical" to listOf(4f, 2f, 0f, 3f, 4f),
            "Vocal Booster" to listOf(-3f, 0f, 5f, 4f, -1f)
        )
        val bands = presets[preset] ?: listOf(0f, 0f, 0f, 0f, 0f)
        _settingsState.value = _settingsState.value.copy(eqPreset = preset, eqBands = bands)
        persistSettings()
    }

    fun onEqBandChanged(index: Int, value: Float) {
        val currentBands = _settingsState.value.eqBands.toMutableList()
        if (index in currentBands.indices) {
            currentBands[index] = value
            _settingsState.value = _settingsState.value.copy(eqPreset = "Custom", eqBands = currentBands)
            persistSettings()
        }
    }

    fun onEqualizerSettingsChanged(enabled: Boolean, preset: String, bands: List<Float>) {
        _settingsState.value = _settingsState.value.copy(
            isEqEnabled = enabled,
            eqPreset = preset,
            eqBands = bands
        )
        persistSettings()
    }
 
     fun onOngoingStreamToggled(enabled: Boolean) {
         _settingsState.value = _settingsState.value.copy(isOngoingStreamEnabled = enabled)
         persistSettings()
     }

     fun onCrossfadeToggled(enabled: Boolean) {
         _settingsState.value = _settingsState.value.copy(isCrossfadeEnabled = enabled)
         persistSettings()
     }

     fun onVolumeNormalizationToggled(enabled: Boolean) {
         _settingsState.value = _settingsState.value.copy(isVolumeNormalizationEnabled = enabled)
         persistSettings()
     }

     fun onCrossfadeDurationChanged(duration: Double) {
         _settingsState.value = _settingsState.value.copy(crossfadeDurationSeconds = duration)
         persistSettings()
     }

     fun onCrossfadeStyleChanged(style: String) {
         _settingsState.value = _settingsState.value.copy(crossfadeStyle = style)
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
