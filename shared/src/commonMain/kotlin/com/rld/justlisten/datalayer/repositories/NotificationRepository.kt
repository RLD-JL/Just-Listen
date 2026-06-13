package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.datalayer.models.FollowNotificationUIModel
import com.rld.justlisten.datalayer.models.NotificationsResponse
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getUserProfile
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope

interface NotificationRepository {
    suspend fun getFollowNotifications(userId: String): List<FollowNotificationUIModel>
}

class NotificationRepositoryImpl(
    private val apiClient: ApiClient
) : NotificationRepository {

    override suspend fun getFollowNotifications(userId: String): List<FollowNotificationUIModel> = coroutineScope {
        try {
            val response: NotificationsResponse? = apiClient.getResponse("/notifications/$userId?types=follow")
            val items = response?.data?.notifications ?: emptyList()
            
            val followActions = items.flatMap { it.actions }
            val followerIds = followActions.mapNotNull { it.data?.followerUserId }.distinct()
            
            val deferredProfiles = followerIds.map { id ->
                async {
                    try {
                        apiClient.getUserProfile(id)
                    } catch (e: Exception) {
                        null
                    }
                }
            }
            val profiles = deferredProfiles.awaitAll().filterNotNull()
            val profilesMap = profiles.associateBy { it.id }
            
            val uiModels = mutableListOf<FollowNotificationUIModel>()
            for (item in items) {
                for (action in item.actions) {
                    val actionData = action.data ?: continue
                    val profile = profilesMap[actionData.followerUserId] ?: continue
                    
                    uiModels.add(
                        FollowNotificationUIModel(
                            followerId = profile.id,
                            followerName = profile.name,
                            followerHandle = profile.handle,
                            followerAvatar = profile.profilePicture?.image150 ?: "",
                            timestamp = action.timestamp.toLong(),
                            isSeen = item.isSeen,
                            isFollowingBack = profile.doesCurrentUserFollow
                        )
                    )
                }
            }
            uiModels.sortByDescending { it.timestamp }
            uiModels
        } catch (e: Exception) {
            co.touchlab.kermit.Logger.e(e) { "Error fetching follow notifications" }
            emptyList()
        }
    }
}
