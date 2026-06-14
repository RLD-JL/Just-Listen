package com.rld.justlisten.datalayer.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CommentUserProfile(
    @SerialName("id") val id: String,
    @SerialName("name") val name: String,
    @SerialName("handle") val handle: String = "",
    @SerialName("profile_picture") val profilePicture: SongIconList? = null,
    @SerialName("is_verified") val isVerified: Boolean = false
)

@Serializable
data class Comment(
    @SerialName("id") val id: String,
    @SerialName("entity_id") val entityId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("message") val message: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("react_count") val reactCount: Int = 0,
    @SerialName("reply_count") val replyCount: Int = 0
)

@Serializable
data class RelatedData(
    @SerialName("users") val users: List<CommentUserProfile> = emptyList()
)

@Serializable
data class TrackCommentsResponse(
    @SerialName("data") val data: List<Comment> = emptyList(),
    @SerialName("related") val related: RelatedData? = null
)

@Serializable
data class CreateCommentRequestBody(
    @SerialName("message") val message: String,
    @SerialName("entity_id") val entityId: String,
    @SerialName("entity_type") val entityType: String = "track"
)
