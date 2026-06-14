package com.rld.justlisten.datalayer.webservices.apis.commentcalls

import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.models.TrackCommentsResponse
import com.rld.justlisten.datalayer.models.CreateCommentRequestBody
import com.rld.justlisten.datalayer.models.ReactCommentRequestBody
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
    val requestBody = CreateCommentRequestBody(
        message = message,
        entityId = trackId,
        parentId = parentId,
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
    return if (react) {
        val requestBody = ReactCommentRequestBody(entityId = trackId)
        postResponse(path, requestBody)
    } else {
        deleteResponse(path)
    }
}
