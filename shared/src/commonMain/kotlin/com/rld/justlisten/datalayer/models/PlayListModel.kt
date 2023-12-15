package com.rld.justlisten.datalayer.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class PlayListModel(
    @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("playlist_name") val playlistTitle: String = "",
    @SerialName("artwork") val songImgList: SongIconList = SongIconList(),
    @SerialName("user") val user: UserModel = UserModel(),
    @SerialName("is_playlist") val isPlaylist: Boolean = false,
    @SerialName("is_streamable") val isStreamable: Boolean = false,
    @Transient val isFavorite: Boolean = false,
    @Transient val songCounter: String = ""
)