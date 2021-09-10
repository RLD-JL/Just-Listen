package com.example.audius.datalayer.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlayListModel (
    @SerialName("artwork") val songImgList : SongIconList = SongIconList(),
    @SerialName("user") val user : UserModel = UserModel(),
    @SerialName("id") val id : String = "",
    @SerialName("playlist_name") val playlistTitle : String = "",
    @SerialName("title") val title : String = "",


    )