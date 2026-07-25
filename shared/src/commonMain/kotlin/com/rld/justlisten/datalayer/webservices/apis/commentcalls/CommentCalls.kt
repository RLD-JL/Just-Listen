package com.rld.justlisten.datalayer.webservices.apis.commentcalls

import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.models.TrackCommentsResponse
import com.rld.justlisten.datalayer.models.CreateCommentRequestBody
import com.rld.justlisten.datalayer.models.ReactCommentRequestBody
import com.rld.justlisten.datalayer.utils.AudiusHashId
import com.rld.justlisten.datalayer.webservices.apis.writecalls.FavoriteResponse

suspend fun ApiClient.getTrackComments(trackId: String, limit: Int = 30, offset: Int = 0): TrackCommentsResponse? {
    return getResponse("/tracks/$trackId/comments?limit=$limit&offset=$offset&sort_method=newest")
}

suspend fun ApiClient.postComment(
    userId: String,
    trackId: String,
    message: String,
    parentId: String? = null,
    trackTimestampS: Int? = null
): FavoriteResponse? {
    val entityId = requireNotNull(AudiusHashId.decode(trackId)) {
        "Invalid Audius track ID"
    }
    val parentCommentId = parentId?.let {
        requireNotNull(AudiusHashId.decode(it)) { "Invalid Audius parent comment ID" }
    }
    val requestBody = CreateCommentRequestBody(
        message = message,
        entityId = entityId,
        entityType = "Track",
        parentId = parentCommentId,
        trackTimestampS = trackTimestampS
    )
    return postResponse("/comments?user_id=$userId", requestBody)
}

suspend fun ApiClient.reactToComment(
    userId: String,
    commentId: String,
    trackId: String,
    react: Boolean
): FavoriteResponse? {
    val path = "/comments/$commentId/react?user_id=$userId"
    val entityId = requireNotNull(AudiusHashId.decode(trackId)) {
        "Invalid Audius track ID"
    }
    val requestBody = ReactCommentRequestBody(
        entityType = "Track",
        entityId = entityId,
    )
    return if (react) {
        postResponse(path, requestBody)
    } else {
        deleteResponse(path, requestBody)
    }
}

suspend fun ApiClient.deleteComment(
    userId: String,
    commentId: String,
): FavoriteResponse? {
    return deleteResponse("/comments/$commentId?user_id=$userId")
}
