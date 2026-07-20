package com.rld.justlisten.navigation

import androidx.compose.ui.unit.dp

/** Layout contract shared by the iOS Compose screen and player hosts. */
internal object IosPlayerLayoutMetrics {
    val miniPlayerHeight = 65.dp

    // UIKit's standard tab-bar content height; the device safe area is added separately.
    val tabBarContentHeight = 49.dp
}
