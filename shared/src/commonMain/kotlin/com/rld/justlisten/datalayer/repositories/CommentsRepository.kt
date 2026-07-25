package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.datalayer.models.TrackCommentsResponse
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.authcalls.UserProfileModel
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserProfile
import com.rld.justlisten.datalayer.webservices.apis.commentcalls.deleteComment
import com.rld.justlisten.datalayer.webservices.apis.commentcalls.getTrackComments
import com.rld.justlisten.datalayer.webservices.apis.commentcalls.postComment
import com.rld.justlisten.datalayer.webservices.apis.commentcalls.reactToComment
import com.rld.justlisten.datalayer.webservices.apis.writecalls.FavoriteResponse

interface CommentsRepository {
    suspend fun getTrackComments(
        trackId: String,
        limit: Int,
        offset: Int,
    ): TrackCommentsResponse?

    suspend fun getUserProfile(userId: String): UserProfileModel?

    suspend fun postComment(
        userId: String,
        trackId: String,
        message: String,
        parentId: String?,
    ): FavoriteResponse?

    suspend fun setCommentReaction(
        userId: String,
        commentId: String,
        trackId: String,
        reacted: Boolean,
    ): FavoriteResponse?

    suspend fun deleteComment(userId: String, commentId: String): FavoriteResponse?
}

class CommentsRepositoryImpl(
    private val apiClient: ApiClient,
) : CommentsRepository {
    override suspend fun getTrackComments(
        trackId: String,
        limit: Int,
        offset: Int,
    ): TrackCommentsResponse? = apiClient.getTrackComments(trackId, limit, offset)

    override suspend fun getUserProfile(userId: String): UserProfileModel? =
        apiClient.getUserProfile(userId)

    override suspend fun postComment(
        userId: String,
        trackId: String,
        message: String,
        parentId: String?,
    ): FavoriteResponse? = apiClient.postComment(userId, trackId, message, parentId)

    override suspend fun setCommentReaction(
        userId: String,
        commentId: String,
        trackId: String,
        reacted: Boolean,
    ): FavoriteResponse? = apiClient.reactToComment(userId, commentId, trackId, reacted)

    override suspend fun deleteComment(
        userId: String,
        commentId: String,
    ): FavoriteResponse? = apiClient.deleteComment(userId, commentId)
}
