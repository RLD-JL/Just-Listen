package com.rld.justlisten.datalayer.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SongIconList (
    @SerialName("150x150") var songImageURL150px : String = "",
    @SerialName("480x480") var songImageURL480px : String = "",
    @SerialName("1000x1000") var songImageURL1000px : String = "",
) {
    init {
        songImageURL150px = songImageURL150px.toReliableAudiusUrl()
        songImageURL480px = songImageURL480px.toReliableAudiusUrl()
        songImageURL1000px = songImageURL1000px.toReliableAudiusUrl()
    }
}

fun String.toReliableAudiusUrl(): String {
    if (this.isBlank()) return ""
    if (this.startsWith("http://") || this.startsWith("https://")) {
        if (this.contains("creatornode2.audius.co")) {
            return this.replace("creatornode2.audius.co", "creatornode.audius.co")
        }
        return this
    }
    val contentIndex = this.indexOf("/content/")
    if (contentIndex != -1) {
        return "https://creatornode.audius.co" + this.substring(contentIndex)
    }
    return this
}

