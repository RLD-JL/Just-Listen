package com.rld.justlisten.datalayer.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object NullableSongIconListSerializer : KSerializer<SongIconList> {
    private val delegate = SongIconList.serializer().nullable
    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: SongIconList) {
        encoder.encodeSerializableValue(delegate, value)
    }

    override fun deserialize(decoder: Decoder): SongIconList {
        return decoder.decodeSerializableValue(delegate) ?: SongIconList()
    }
}

@Serializable
data class RepostModel(
    @SerialName("repost_item_id") val repostItemId: String = "",
    @SerialName("repost_type") val repostType: String = "",
    @SerialName("user_id") val userId: String = ""
)

@Serializable
data class PlayListModel(
    @SerialName("id") val id: String = "",
    @SerialName("title") val title: String = "",
    @SerialName("playlist_name") val playlistTitle: String = "",
    @Serializable(with = NullableSongIconListSerializer::class)
    @SerialName("artwork") val songImgList: SongIconList = SongIconList(),
    @SerialName("user") val user: UserModel = UserModel(),
    @SerialName("is_playlist") val isPlaylist: Boolean = false,
    @SerialName("is_streamable") val isStreamable: Boolean = false,
    @SerialName("repost_count") val repostCount: Int = 0,
    @SerialName("favorite_count") val favoriteCount: Int = 0,
    @SerialName("has_current_user_reposted") val hasCurrentUserReposted: Boolean = false,
    @Transient val isFavorite: Boolean = false,
    @Transient val songCounter: String = "",
    @Transient val durationPlayedSec: Long = 0L,
    @SerialName("is_album") val isAlbum: Boolean = false,
    @SerialName("play_count") val playCount: Int = 0,
    @SerialName("total_play_count") val totalPlayCount: Int = 0,
    @SerialName("comment_count") val commentCount: Int = 0,
    @SerialName("duration") val duration: Int = 0,
    @SerialName("release_date") val releaseDate: String? = null,
    @SerialName("followee_reposts") val followeeReposts: List<RepostModel> = emptyList()
)