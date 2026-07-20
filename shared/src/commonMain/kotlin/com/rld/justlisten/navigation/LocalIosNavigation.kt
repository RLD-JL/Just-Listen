package com.rld.justlisten.navigation

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

data class IosNavigationCallbacks(
    val onNavigate: (Route) -> Unit,
    val onPopBackStack: () -> Unit
)

val LocalIosNavigationCallbacks = staticCompositionLocalOf<IosNavigationCallbacks?> { null }
val LocalUseNativeNavigation = staticCompositionLocalOf { false }
val LocalNativeBottomOverlayPadding = staticCompositionLocalOf { 0.dp }
