package com.rld.justlisten.datalayer.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SongIconList (
    @SerialName("150x150") val songImageURL150px : String = "",
    @SerialName("480x480") val songImageURL480px : String = "",
    @SerialName("1000x1000") val songImageURL1000px : String = "",
)

