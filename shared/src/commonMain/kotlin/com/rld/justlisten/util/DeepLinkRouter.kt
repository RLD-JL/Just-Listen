package com.rld.justlisten.util

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

data class JustListenDeepLink(
    val path: String,
    val parameters: Map<String, String>,
)

fun parseJustListenDeepLink(url: String): JustListenDeepLink? {
    val cleanUrl = url.substringBefore('#')

    if (cleanUrl.startsWith("https://justlisten.cloud/comments/", ignoreCase = true)) {
        val pathSegments = cleanUrl
            .substringAfter("https://justlisten.cloud/comments/", missingDelimiterValue = "")
            .substringBefore('?')
            .trim('/')
            .split('/')
            .filter(String::isNotBlank)
        val trackId = pathSegments.getOrNull(0) ?: return null
        if (pathSegments.size > 2) return null
        return JustListenDeepLink(
            path = "comments/share",
            parameters = buildMap {
                put("trackId", trackId)
                pathSegments.getOrNull(1)?.let { put("commentId", it) }
            },
        )
    }

    if (cleanUrl.startsWith("https://justlisten.cloud/track/", ignoreCase = true)) {
        val trackId = cleanUrl
            .substringAfter("https://justlisten.cloud/track/", missingDelimiterValue = "")
            .substringBefore('?')
            .trim('/')
        return trackId.takeIf { it.isNotBlank() }?.let {
            JustListenDeepLink(path = "track/share", parameters = mapOf("id" to it))
        }
    }

    if (!cleanUrl.startsWith("justlisten://", ignoreCase = true)) return null

    val path = cleanUrl.substringAfter("justlisten://").substringBefore('?')
    val query = cleanUrl.substringAfter('?', missingDelimiterValue = "")
    val parameters = if (query.isBlank()) {
        emptyMap()
    } else {
        query.split('&').mapNotNull { item ->
            val parts = item.split('=', limit = 2)
            parts.takeIf { it.size == 2 }?.let { it[0] to it[1] }
        }.toMap()
    }
    return JustListenDeepLink(path = path, parameters = parameters)
}

fun commentsShareUrl(trackId: String, commentId: String? = null): String = buildString {
    append("https://justlisten.cloud/comments/")
    append(trackId)
    if (!commentId.isNullOrBlank()) {
        append('/')
        append(commentId)
    }
}

object DeepLinkRouter {
    private val pendingDeepLinks = Channel<String>(capacity = Channel.BUFFERED)
    val deepLinkFlow = pendingDeepLinks.receiveAsFlow()

    fun handleDeepLink(url: String) {
        pendingDeepLinks.trySend(url)
    }
}
