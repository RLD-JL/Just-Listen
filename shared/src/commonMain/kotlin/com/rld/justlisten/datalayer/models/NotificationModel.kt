package com.rld.justlisten.datalayer.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FollowNotificationActionData(
    @SerialName("follower_user_id") val followerUserId: String,
    @SerialName("followee_user_id") val followeeUserId: String
)

@Serializable
data class FollowNotificationAction(
    @SerialName("type") val type: String = "",
    @SerialName("timestamp") val timestamp: Double = 0.0,
    @SerialName("data") val data: FollowNotificationActionData? = null
)

@Serializable
data class NotificationItem(
    @SerialName("type") val type: String,
    @SerialName("group_id") val groupId: String = "",
    @SerialName("is_seen") val isSeen: Boolean = false,
    @SerialName("actions") val actions: List<FollowNotificationAction> = emptyList()
)

@Serializable
data class NotificationsData(
    @SerialName("unread_count") val unreadCount: Int = 0,
    @SerialName("notifications") val notifications: List<NotificationItem> = emptyList()
)

@Serializable
data class NotificationsResponse(
    @SerialName("data") val data: NotificationsData? = null
)

// A custom model for display purposes combining user details with follow notification data
data class FollowNotificationUIModel(
    val followerId: String,
    val followerName: String,
    val followerHandle: String,
    val followerAvatar: String,
    val timestamp: Long,
    val isSeen: Boolean,
    var isFollowingBack: Boolean = false
)
