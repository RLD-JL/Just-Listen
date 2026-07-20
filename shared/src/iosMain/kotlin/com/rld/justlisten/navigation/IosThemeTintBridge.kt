package com.rld.justlisten.navigation

import com.rld.justlisten.viewmodel.screens.settings.SettingsState
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.mp.KoinPlatform
import platform.UIKit.UIColor
import platform.UIKit.UIViewController

private fun SettingsState.nativePrimaryHex(): String = when (palletColor) {
    "Green" -> "388E67"
    "Purple" -> "502DA8"
    "Orange" -> "FE3122"
    "Blue" -> "2750CC"
    "Pink" -> "FF98A9"
    "Custom" -> customPrimary
        ?.trim()
        ?.removePrefix("#")
        ?.takeIf { it.length == 6 || it.length == 8 }
        ?: if (isDarkThemeOn) "ECE8E8" else "111111"
    else -> if (isDarkThemeOn) "ECE8E8" else "111111"
}

private class IosThemeTintObserverController(
    private val onTintChanged: (String) -> Unit,
) : UIViewController(nibName = null, bundle = null) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.clearColor

        val settingsViewModel = KoinPlatform.getKoin().get<SettingsViewModel>()
        scope.launch {
            settingsViewModel.settingsState
                .map(SettingsState::nativePrimaryHex)
                .distinctUntilChanged()
                .collect(onTintChanged)
        }
    }
}

// A UIKit observer lets the SwiftUI navigation shell follow the Compose theme
// without introducing another Compose/Metal surface.
fun ThemeTintObserverViewController(
    onTintChanged: (String) -> Unit,
): UIViewController = IosThemeTintObserverController(onTintChanged)
