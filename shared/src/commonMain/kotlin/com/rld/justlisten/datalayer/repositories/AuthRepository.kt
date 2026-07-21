package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.ApiRequestException
import com.rld.justlisten.datalayer.webservices.apis.authcalls.MeResponse
import com.rld.justlisten.datalayer.webservices.apis.authcalls.exchangeCodeForTokens
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getMe
import com.rld.justlisten.util.PkceCrypto
import com.rld.justlisten.util.SecureStorage
import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.webservices.apis.authcalls.ProfileImages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


sealed interface SessionState {
    object Guest : SessionState
    object Restoring : SessionState
    data class Authenticated(val userProfile: MeResponse) : SessionState
}

interface AuthRepository {
    val sessionState: StateFlow<SessionState>
    fun getAuthUrl(redirectUri: String): String
    suspend fun loginWithCode(code: String, redirectUri: String): Boolean
    suspend fun refreshSession(): Boolean
    fun logout()
    fun getCustomName(userId: String): String?
    fun getCustomBio(userId: String): String?
    fun getCustomProfilePic(userId: String): String?
    fun getCustomCoverPhoto(userId: String): String?
    fun getCustomLocation(userId: String): String?
    fun getCustomXHandle(userId: String): String?
    fun getCustomInstagramHandle(userId: String): String?
    fun getCustomTikTokHandle(userId: String): String?
    fun getCustomWebsite(userId: String): String?
    fun getCustomFanClubFlair(userId: String): String?
    fun updateUserProfile(
        userId: String,
        name: String,
        bio: String?,
        profilePicUrl: String?,
        coverPhotoUrl: String?,
        location: String?,
        xHandle: String?,
        instagramHandle: String?,
        tiktokHandle: String?,
        website: String?,
        fanClubFlair: String?
    )
}

class AuthRepositoryImpl(
    private val apiClient: ApiClient,
    private val secureStorage: SecureStorage,
    private val pkceCrypto: PkceCrypto,
    private val syncRepository: SyncRepository,
    private val localDb: LocalDb,
    private val clientID: String = apiClient.apiKey
) : AuthRepository {

    private val _sessionState = MutableStateFlow(restoredSessionState())
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private var currentVerifier: String? = null
    private val repositoryScope = CoroutineScope(Dispatchers.Default + kotlinx.coroutines.SupervisorJob())

    private fun restoredSessionState(): SessionState {
        if (secureStorage.getToken("access_token").isNullOrBlank()) return SessionState.Guest

        val userId = secureStorage.getToken("cached_user_id") ?: return SessionState.Restoring
        val name = secureStorage.getToken("cached_user_name") ?: return SessionState.Restoring
        val handle = secureStorage.getToken("cached_user_handle") ?: return SessionState.Restoring
        val profilePicture = secureStorage.getToken("cached_user_profile_picture")

        return SessionState.Authenticated(
            MeResponse(
                userId = userId,
                name = name,
                handle = handle,
                verified = secureStorage.getToken("cached_user_verified") == "true",
                profilePicture = profilePicture?.let {
                    ProfileImages(image150 = it, image480 = it, image1000 = it)
                },
            )
        )
    }

    private fun cacheProfile(profile: MeResponse) {
        profile.userId?.takeIf { it.isNotBlank() }?.let {
            secureStorage.saveToken("user_id", it)
            secureStorage.saveToken("cached_user_id", it)
        }
        secureStorage.saveToken("cached_user_name", profile.name)
        secureStorage.saveToken("cached_user_handle", profile.handle)
        secureStorage.saveToken("cached_user_verified", profile.verified.toString())
        profile.profilePicture?.image150?.takeIf { it.isNotBlank() }?.let {
            secureStorage.saveToken("cached_user_profile_picture", it)
        }
    }

    private fun publishAuthenticated(profile: MeResponse) {
        cacheProfile(profile)
        _sessionState.value = SessionState.Authenticated(profile)
        profile.userId?.takeIf { it.isNotBlank() }?.let { userId ->
            repositoryScope.launch {
                syncRepository.performInboundSync(userId)
            }
        }
    }

    private fun preserveSessionAfter(exception: Throwable) {
        val credentialsWereRejected = exception is ApiRequestException &&
            !exception.isTransient && exception.statusCode in 400..499
        if (credentialsWereRejected) {
            _sessionState.value = SessionState.Guest
        } else if (_sessionState.value !is SessionState.Authenticated) {
            _sessionState.value = SessionState.Restoring
        }
    }

    override fun getAuthUrl(redirectUri: String): String {
        val verifier = pkceCrypto.generateCodeVerifier()
        currentVerifier = verifier
        secureStorage.saveToken("code_verifier", verifier)
        val challenge = pkceCrypto.generateCodeChallenge(verifier)
        return "https://audius.co/oauth/auth" +
                "?client_id=$clientID" +
                "&redirect_uri=$redirectUri" +
                "&scope=write" +
                "&response_type=code" +
                "&code_challenge=$challenge" +
                "&code_challenge_method=S256"
    }

    override suspend fun loginWithCode(code: String, redirectUri: String): Boolean {
        var credentialsStored = false
        return try {
            val verifier = currentVerifier ?: secureStorage.getToken("code_verifier") ?: return false
            val tokenResponse = apiClient.exchangeCodeForTokens(code, verifier, redirectUri) ?: return false
            secureStorage.saveToken("access_token", tokenResponse.accessToken)
            secureStorage.saveToken("refresh_token", tokenResponse.refreshToken)
            credentialsStored = true
            // Token exchange and profile loading are separate network requests.
            // Publish the intermediate state so a transient /me failure can be
            // retried without requiring the app to restart.
            _sessionState.value = SessionState.Restoring

            // Fetch user profile
            val userProfile = apiClient.getMe()
            if (userProfile != null) {
                val userId = userProfile.userId
                if (!userId.isNullOrBlank()) {
                    secureStorage.saveToken("user_id", userId)
                }
                
                val override = if (!userId.isNullOrBlank()) {
                    localDb.settingsScreenQueries.getUserProfileOverride(userId).executeAsOneOrNull()
                } else null
                
                val finalProfile = if (override != null) {
                    userProfile.copy(
                        name = override.customName ?: userProfile.name,
                        profilePicture = if (!override.customProfilePic.isNullOrBlank()) {
                            ProfileImages(
                                image150 = override.customProfilePic,
                                image480 = override.customProfilePic,
                                image1000 = override.customProfilePic
                            )
                        } else userProfile.profilePicture
                    )
                } else {
                    userProfile
                }

                publishAuthenticated(finalProfile)
                true
            } else {
                false
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Throwable) {
            co.touchlab.kermit.Logger.e(exception) { "AuthRepository: loginWithCode failed" }
            if (credentialsStored) preserveSessionAfter(exception)
            false
        }
    }

    override suspend fun refreshSession(): Boolean {
        return try {
            val accessToken = secureStorage.getToken("access_token")
            if (accessToken.isNullOrBlank()) {
                _sessionState.value = SessionState.Guest
                return false
            }
            if (_sessionState.value !is SessionState.Authenticated) {
                _sessionState.value = SessionState.Restoring
            }
            val userProfile = apiClient.getMe()
            if (userProfile != null) {
                val userId = userProfile.userId
                if (!userId.isNullOrBlank()) {
                    secureStorage.saveToken("user_id", userId)
                }
                
                val override = if (!userId.isNullOrBlank()) {
                    localDb.settingsScreenQueries.getUserProfileOverride(userId).executeAsOneOrNull()
                } else null
                
                val finalProfile = if (override != null) {
                    userProfile.copy(
                        name = override.customName ?: userProfile.name,
                        profilePicture = if (!override.customProfilePic.isNullOrBlank()) {
                            ProfileImages(
                                image150 = override.customProfilePic,
                                image480 = override.customProfilePic,
                                image1000 = override.customProfilePic
                            )
                        } else userProfile.profilePicture
                    )
                } else {
                    userProfile
                }

                publishAuthenticated(finalProfile)
                true
            } else {
                if (_sessionState.value !is SessionState.Authenticated) {
                    _sessionState.value = SessionState.Restoring
                }
                false
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Throwable) {
            co.touchlab.kermit.Logger.e(exception) { "AuthRepository: refreshSession failed" }
            preserveSessionAfter(exception)
            false
        }
    }

    override fun logout() {
        secureStorage.clear()
        _sessionState.value = SessionState.Guest
    }

    override fun getCustomName(userId: String): String? {
        return localDb.settingsScreenQueries.getUserProfileOverride(userId).executeAsOneOrNull()?.customName
    }

    override fun getCustomBio(userId: String): String? {
        return localDb.settingsScreenQueries.getUserProfileOverride(userId).executeAsOneOrNull()?.customBio
    }

    override fun getCustomProfilePic(userId: String): String? {
        return localDb.settingsScreenQueries.getUserProfileOverride(userId).executeAsOneOrNull()?.customProfilePic
    }

    override fun getCustomCoverPhoto(userId: String): String? {
        return localDb.settingsScreenQueries.getUserProfileOverride(userId).executeAsOneOrNull()?.customCoverPhoto
    }

    override fun getCustomLocation(userId: String): String? {
        return localDb.settingsScreenQueries.getUserProfileOverride(userId).executeAsOneOrNull()?.customLocation
    }

    override fun getCustomXHandle(userId: String): String? {
        return localDb.settingsScreenQueries.getUserProfileOverride(userId).executeAsOneOrNull()?.xHandle
    }

    override fun getCustomInstagramHandle(userId: String): String? {
        return localDb.settingsScreenQueries.getUserProfileOverride(userId).executeAsOneOrNull()?.instagramHandle
    }

    override fun getCustomTikTokHandle(userId: String): String? {
        return localDb.settingsScreenQueries.getUserProfileOverride(userId).executeAsOneOrNull()?.tiktokHandle
    }

    override fun getCustomWebsite(userId: String): String? {
        return localDb.settingsScreenQueries.getUserProfileOverride(userId).executeAsOneOrNull()?.website
    }

    override fun getCustomFanClubFlair(userId: String): String? {
        return localDb.settingsScreenQueries.getUserProfileOverride(userId).executeAsOneOrNull()?.fanClubFlair
    }

    override fun updateUserProfile(
        userId: String,
        name: String,
        bio: String?,
        profilePicUrl: String?,
        coverPhotoUrl: String?,
        location: String?,
        xHandle: String?,
        instagramHandle: String?,
        tiktokHandle: String?,
        website: String?,
        fanClubFlair: String?
    ) {
        localDb.settingsScreenQueries.transaction {
            localDb.settingsScreenQueries.upsertUserProfileOverride(
                userId = userId,
                customName = name,
                customBio = bio,
                customProfilePic = profilePicUrl,
                customCoverPhoto = coverPhotoUrl,
                customLocation = location,
                xHandle = xHandle,
                instagramHandle = instagramHandle,
                tiktokHandle = tiktokHandle,
                website = website,
                fanClubFlair = fanClubFlair
            )
        }

        val currentSession = _sessionState.value
        if (currentSession is SessionState.Authenticated && currentSession.userProfile.userId == userId) {
            val updatedProfile = currentSession.userProfile.copy(
                name = name,
                profilePicture = if (!profilePicUrl.isNullOrBlank()) {
                    ProfileImages(
                        image150 = profilePicUrl,
                        image480 = profilePicUrl,
                        image1000 = profilePicUrl
                    )
                } else currentSession.userProfile.profilePicture
            )
            _sessionState.value = SessionState.Authenticated(updatedProfile)
        }
    }
}
