package com.rld.justlisten.datalayer.webservices.apis.commentcalls

import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.models.TrackCommentsResponse
import com.rld.justlisten.datalayer.models.CreateCommentRequestBody
import com.rld.justlisten.datalayer.webservices.apis.writecalls.FavoriteResponse

suspend fun ApiClient.getTrackComments(trackId: String, limit: Int = 30, offset: Int = 0): TrackCommentsResponse? {
    return getResponse("/tracks/$trackId/comments?limit=$limit&offset=$offset&sort_method=newest")
}

suspend fun ApiClient.postComment(userId: String, trackId: String, message: String): FavoriteResponse? {
    val requestBody = CreateCommentRequestBody(message = message, entityId = trackId)
    return postResponse("/comments?user_id=$userId", requestBody)
}
