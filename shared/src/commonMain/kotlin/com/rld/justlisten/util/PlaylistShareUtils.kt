package com.rld.justlisten.util

import com.rld.justlisten.navigation.Route
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

object PlaylistShareUtils {
    private val json = Json { ignoreUnknownKeys = true }

    fun exportPlaylist(playlist: Route.PlaylistDetail): String {
        val jsonStr = json.encodeToString(playlist)
        val bytes = jsonStr.encodeToByteArray()
        val compressed = gzipCompress(bytes)
        return base64UrlEncode(compressed)
    }

    fun importPlaylist(base64Data: String): Route.PlaylistDetail? {
        return try {
            val compressed = base64UrlDecode(base64Data)
            val decompressed = gzipDecompress(compressed)
            val jsonStr = decompressed.decodeToString()
            json.decodeFromString<Route.PlaylistDetail>(jsonStr)
        } catch (e: Exception) {
            null
        }
    }
}
