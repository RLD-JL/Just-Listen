package com.rld.justlisten.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import com.rld.justlisten.ui.settingsscreen.SettingsScreen
import com.rld.justlisten.ui.supportscreen.SupportScreen
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import org.koin.compose.koinInject

@Composable
fun SettingsScreenHost(navController: NavHostController) {
    val viewModel: SettingsViewModel = koinInject()
    val state by viewModel.settingsState.collectAsState()
    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
    val iosCallbacks = LocalIosNavigationCallbacks.current

    CollectNavigationEvents(viewModel, navController)

    SettingsScreen(
        settings = state,
        updateSettings = { updated ->
            if (updated.isDarkThemeOn != state.isDarkThemeOn) {
                viewModel.onDarkModeToggled(updated.isDarkThemeOn)
            }
            if (updated.hasSupportNavigationOn != state.hasSupportNavigationOn) {
                viewModel.onSupportToggled(updated.hasSupportNavigationOn)
            }
            if (updated.palletColor != state.palletColor) {
                viewModel.onPaletteSelected(updated.palletColor)
            }
            if (updated.isOngoingStreamEnabled != state.isOngoingStreamEnabled) {
                viewModel.onOngoingStreamToggled(updated.isOngoingStreamEnabled)
            }
            if (updated.isCrossfadeEnabled != state.isCrossfadeEnabled) {
                viewModel.onCrossfadeToggled(updated.isCrossfadeEnabled)
            }
            if (updated.isVolumeNormalizationEnabled != state.isVolumeNormalizationEnabled) {
                viewModel.onVolumeNormalizationToggled(updated.isVolumeNormalizationEnabled)
            }
            if (updated.useLiquidGlassNavigation != state.useLiquidGlassNavigation) {
                viewModel.onLiquidGlassNavigationToggled(updated.useLiquidGlassNavigation)
            }
            if (updated.crossfadeDurationSeconds != state.crossfadeDurationSeconds) {
                viewModel.onCrossfadeDurationChanged(updated.crossfadeDurationSeconds)
            }
            if (updated.crossfadeStyle != state.crossfadeStyle) {
                viewModel.onCrossfadeStyleChanged(updated.crossfadeStyle)
            }
            if (updated.isEqEnabled != state.isEqEnabled ||
                updated.eqPreset != state.eqPreset ||
                updated.eqBands != state.eqBands
            ) {
                viewModel.onEqualizerSettingsChanged(
                    enabled = updated.isEqEnabled,
                    preset = updated.eqPreset,
                    bands = updated.eqBands
                )
            }
        },
        previewEqualizerSettings = viewModel::previewEqualizerSettings,
        saveEqualizerSettings = viewModel::saveEqualizerPreview,
        cancelEqualizerPreview = viewModel::cancelEqualizerPreview,
        onNavigateToCustomTheme = {
            iosCallbacks?.onNavigate?.invoke(Route.CustomTheme)
                ?: navController.navigate(Route.CustomTheme)
        },
        onLogin = { redirectUri ->
            val authUrl = viewModel.getAuthUrl(redirectUri)
            if (authUrl.isNotBlank()) {
                uriHandler.openUri(authUrl)
            }
        },
        onLogout = viewModel::logout,
        onRetrySync = viewModel::retryFailedSync,
        onClearSync = viewModel::clearFailedSync,
        onNavigateToMyProfile = { userId, name ->
            val route = Route.ArtistProfile(userId, name)
            iosCallbacks?.onNavigate?.invoke(route)
                ?: navController.navigate(route)
        }
    )
}

@Composable
fun SupportScreenHost(navController: NavHostController) {
    SupportScreen()
}

@Composable
fun CustomThemeScreenHost(navController: NavHostController) {
    val viewModel: SettingsViewModel = koinInject()
    val state by viewModel.settingsState.collectAsState()
    val iosCallbacks = LocalIosNavigationCallbacks.current

    com.rld.justlisten.ui.settingsscreen.CustomThemeScreen(
        settings = state,
        onBackPressed = {
            iosCallbacks?.onPopBackStack?.invoke()
                ?: navController.popBackStack()
        },
        onCustomColorsApplied = viewModel::updateCustomColors,
        onPaletteSelected = viewModel::onPaletteSelected
    )
}
