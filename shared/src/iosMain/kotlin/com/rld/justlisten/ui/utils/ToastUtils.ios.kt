@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
package com.rld.justlisten.ui.utils

import kotlinx.cinterop.useContents
import platform.UIKit.*
import platform.CoreGraphics.CGRectMake
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue
import co.touchlab.kermit.Logger

actual fun showToast(message: String) {
    Logger.i { "iOS Toast: $message" }
    dispatch_async(dispatch_get_main_queue()) {
        val window = UIApplication.sharedApplication.connectedScenes
            .firstOrNull { it is UIWindowScene }
            ?.let { scene -> (scene as UIWindowScene).windows.firstOrNull { (it as? UIWindow)?.isKeyWindow() == true } as? UIWindow }
        val view = window?.rootViewController?.view ?: return@dispatch_async
        
        val screenBounds = UIScreen.mainScreen.bounds
        val width = screenBounds.useContents { size.width }
        val height = screenBounds.useContents { size.height }
        
        val toastLabel = UILabel().apply {
            backgroundColor = UIColor.blackColor.colorWithAlphaComponent(0.8)
            textColor = UIColor.whiteColor
            textAlignment = NSTextAlignmentCenter
            font = UIFont.systemFontOfSize(14.0)
            text = message
            alpha = 0.0
            layer.cornerRadius = 10.0
            clipsToBounds = true
        }
        
        val toastWidth = (width - 40.0).coerceAtMost(300.0)
        val toastHeight = 45.0
        val toastX = (width - toastWidth) / 2.0
        val safeBottom = view.safeAreaInsets.useContents { bottom }
        val toastY = height - 80.0 - safeBottom
        
        toastLabel.setFrame(CGRectMake(toastX, toastY, toastWidth, toastHeight))
        view.addSubview(toastLabel)
        
        UIView.animateWithDuration(
            duration = 0.3,
            animations = {
                toastLabel.alpha = 1.0
            },
            completion = { _ ->
                UIView.animateWithDuration(
                    duration = 0.3,
                    delay = 2.0,
                    options = 0UL,
                    animations = {
                        toastLabel.alpha = 0.0
                    },
                    completion = { _ ->
                        toastLabel.removeFromSuperview()
                    }
                )
            }
        )
    }
}
