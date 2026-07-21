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
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val apiClient: ApiClient,
) : BaseScreenViewModel() {
    private data class EqualizerDraft(
        val enabled: Boolean,
        val preset: String,
        val bands: List<Float>,
    )

    private var equalizerPreviewOriginal: EqualizerDraft? = null
    private var sessionRestorationJob: Job? = null
    
    private val _settingsState = MutableStateFlow(SettingsState())
    val settingsState: StateFlow<SettingsState> = _settingsState.asStateFlow()

    init {
        loadSettings()

        // Check/refresh an existing session on launch. Guest is already a
        // resolved state, so it does not need a network request.
        if (authRepository.sessionState.value !is SessionState.Guest) {
            startSessionRestoration()
        }

        // Collect session state
        viewModelScope.launch {
            authRepository.sessionState.collect { session ->
                _settingsState.value = _settingsState.value.copy(
                    sessionState = session,
                    isSessionRecoveryExhausted = if (session is SessionState.Restoring) {
                        _settingsState.value.isSessionRecoveryExhausted
                    } else {
                        false
                    },
                )
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
                val blocked = withContext(Dispatchers.IO) {
                    settingsRepository.getBlockedUsers()
                }
                val hidden = withContext(Dispatchers.IO) {
                    settingsRepository.getHiddenComments()
                }
                val bands = try {
                    saved.eqBands.split(",").map { it.toFloat() }
                } catch (e: Exception) {
                    listOf(0f, 0f, 0f, 0f, 0f)
                }
                _settingsState.value = _settingsState.value.copy(
                    hasSupportNavigationOn = saved.hasNavigationSupportOn,
                    blockedUsers = blocked,
                    hiddenComments = hidden,
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
                    useLiquidGlassNavigation = com.rld.justlisten.ui.utils.isLiquidGlassNavigationEnabled(),
                    isSettingsLoaded = true
                )
            } catch (_: Exception) {
                // First run — use defaults
                _settingsState.value = _settingsState.value.copy(
                    useLiquidGlassNavigation = com.rld.justlisten.ui.utils.isLiquidGlassNavigationEnabled(),
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
        com.rld.justlisten.ui.utils.setLiquidGlassNavigationEnabled(state.useLiquidGlassNavigation)
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.saveSettingsInfo(
                hasNavigationSupportOn = state.hasSupportNavigationOn,
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
        saveEqualizerPreview(enabled, preset, bands)
    }

    fun previewEqualizerSettings(enabled: Boolean, preset: String, bands: List<Float>) {
        if (equalizerPreviewOriginal == null) {
            val current = _settingsState.value
            equalizerPreviewOriginal = EqualizerDraft(
                enabled = current.isEqEnabled,
                preset = current.eqPreset,
                bands = current.eqBands.toList(),
            )
        }
        _settingsState.value = _settingsState.value.copy(
            isEqEnabled = enabled,
            eqPreset = preset,
            eqBands = bands.toList(),
        )
    }

    fun cancelEqualizerPreview() {
        val original = equalizerPreviewOriginal ?: return
        equalizerPreviewOriginal = null
        _settingsState.value = _settingsState.value.copy(
            isEqEnabled = original.enabled,
            eqPreset = original.preset,
            eqBands = original.bands,
        )
    }

    fun saveEqualizerPreview(enabled: Boolean, preset: String, bands: List<Float>) {
        equalizerPreviewOriginal = null
        _settingsState.value = _settingsState.value.copy(
            isEqEnabled = enabled,
            eqPreset = preset,
            eqBands = bands.toList(),
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

    fun onLiquidGlassNavigationToggled(enabled: Boolean) {
        _settingsState.value = _settingsState.value.copy(useLiquidGlassNavigation = enabled)
        persistSettings()
    }

    fun loginWithCode(code: String, redirectUri: String) {
        sessionRestorationJob?.cancel()
        sessionRestorationJob = null
        viewModelScope.launch {
            _settingsState.value = _settingsState.value.copy(
                isLoading = true,
                isSessionRecoveryExhausted = false,
            )
            val success = authRepository.loginWithCode(code, redirectUri)
            _settingsState.value = _settingsState.value.copy(isLoading = false)
            if (success) {
                completeOnboarding()
            } else if (authRepository.sessionState.value is SessionState.Restoring) {
                startSessionRestoration()
            }
        }
    }

    fun retrySessionRestoration() {
        if (authRepository.sessionState.value is SessionState.Restoring) {
            startSessionRestoration()
        }
    }

    private fun startSessionRestoration() {
        if (sessionRestorationJob?.isActive == true) return

        _settingsState.value = _settingsState.value.copy(isSessionRecoveryExhausted = false)
        sessionRestorationJob = viewModelScope.launch {
            for (delayMs in SESSION_RESTORE_DELAYS_MS) {
                if (authRepository.sessionState.value !is SessionState.Restoring) return@launch
                if (delayMs > 0L) delay(delayMs)
                if (authRepository.sessionState.value !is SessionState.Restoring) return@launch

                authRepository.refreshSession()
                if (authRepository.sessionState.value !is SessionState.Restoring) return@launch
            }

            if (authRepository.sessionState.value is SessionState.Restoring) {
                _settingsState.value = _settingsState.value.copy(
                    isSessionRecoveryExhausted = true,
                )
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
    
    fun onSupportToggled(hasSupportNavigationOn: Boolean) {
        _settingsState.value = _settingsState.value.copy(hasSupportNavigationOn = hasSupportNavigationOn)
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

    fun blockUser(userId: String, username: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                settingsRepository.blockUser(userId, username)
            }
            val blocked = withContext(Dispatchers.IO) {
                settingsRepository.getBlockedUsers()
            }
            _settingsState.value = _settingsState.value.copy(blockedUsers = blocked)
        }
    }

    fun unblockUser(userId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                settingsRepository.unblockUser(userId)
            }
            val blocked = withContext(Dispatchers.IO) {
                settingsRepository.getBlockedUsers()
            }
            _settingsState.value = _settingsState.value.copy(blockedUsers = blocked)
        }
    }

    fun hideComment(commentId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                settingsRepository.hideComment(commentId)
            }
            val hidden = withContext(Dispatchers.IO) {
                settingsRepository.getHiddenComments()
            }
            _settingsState.value = _settingsState.value.copy(hiddenComments = hidden)
        }
    }

    fun unhideComment(commentId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                settingsRepository.unhideComment(commentId)
            }
            val hidden = withContext(Dispatchers.IO) {
                settingsRepository.getHiddenComments()
            }
            _settingsState.value = _settingsState.value.copy(hiddenComments = hidden)
        }
    }
}

private val SESSION_RESTORE_DELAYS_MS = longArrayOf(0L, 1_000L, 2_000L, 4_000L, 8_000L)
