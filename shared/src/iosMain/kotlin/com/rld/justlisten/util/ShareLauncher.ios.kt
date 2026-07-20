@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.rld.justlisten.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIActivityViewController
import platform.UIKit.UINavigationController
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import platform.UIKit.UIApplication
import platform.UIKit.popoverPresentationController

private class IosShareLauncher : ShareLauncher {
    override fun share(text: String, title: String) {
        val presenter = topViewController(keyWindow()?.rootViewController) ?: return
        val shareSheet = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )

        shareSheet.popoverPresentationController?.let { popover ->
            popover.sourceView = presenter.view
            popover.sourceRect = presenter.view.bounds
        }

        presenter.presentViewController(shareSheet, animated = true, completion = null)
    }
}

private fun keyWindow(): UIWindow? = UIApplication.sharedApplication.connectedScenes
    .mapNotNull { it as? UIWindowScene }
    .flatMap { it.windows }
    .mapNotNull { it as? UIWindow }
    .firstOrNull { it.isKeyWindow() }

private tailrec fun topViewController(controller: UIViewController?): UIViewController? {
    val nextController = when {
        controller?.presentedViewController != null -> controller.presentedViewController
        controller is UINavigationController -> controller.visibleViewController
        controller is UITabBarController -> controller.selectedViewController
        else -> null
    }
    return if (nextController == null) controller else topViewController(nextController)
}

@Composable
actual fun rememberShareLauncher(): ShareLauncher = remember { IosShareLauncher() }
