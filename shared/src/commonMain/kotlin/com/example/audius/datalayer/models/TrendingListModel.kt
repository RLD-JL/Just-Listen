package com.example.audius.datalayer.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrendingListModel (
    @SerialName("id") val id : String = "",
    @SerialName("title") val title : String = "",
    @SerialName("favorite_count") val favouriteCount : Int = 0,
    @SerialName("repost_count") val repostCount : Int = 0,
    @SerialName("artwork") val songImgList : SongIconList = SongIconList(),
)