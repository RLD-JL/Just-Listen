package com.rld.justlisten.util

import kotlinx.coroutines.flow.MutableSharedFlow

object DeepLinkRouter {
    val deepLinkFlow = MutableSharedFlow<String>(extraBufferCapacity = 10)

    fun handleDeepLink(url: String) {
        deepLinkFlow.tryEmit(url)
    }
}
