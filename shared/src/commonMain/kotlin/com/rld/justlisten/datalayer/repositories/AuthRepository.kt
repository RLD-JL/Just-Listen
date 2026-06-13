package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.authcalls.MeResponse
import com.rld.justlisten.datalayer.webservices.apis.authcalls.exchangeCodeForTokens
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getMe
import com.rld.justlisten.util.PkceCrypto
import com.rld.justlisten.util.SecureStorage
import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.webservices.apis.authcalls.ProfileImages
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


sealed interface SessionState {
    object Guest : SessionState
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
    fun updateUserProfile(userId: String, name: String, bio: String?, profilePicUrl: String?, coverPhotoUrl: String?)
}

class AuthRepositoryImpl(
    private val apiClient: ApiClient,
    private val secureStorage: SecureStorage,
    private val pkceCrypto: PkceCrypto,
    private val syncRepository: SyncRepository,
    private val localDb: LocalDb,
    private val clientID: String = apiClient.apiKey
) : AuthRepository {

    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Guest)
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    private var currentVerifier: String? = null

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
        return runCatching {
            val verifier = currentVerifier ?: secureStorage.getToken("code_verifier") ?: return false
            val tokenResponse = apiClient.exchangeCodeForTokens(code, verifier, redirectUri) ?: return false
            secureStorage.saveToken("access_token", tokenResponse.accessToken)
            secureStorage.saveToken("refresh_token", tokenResponse.refreshToken)
            
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

                _sessionState.value = SessionState.Authenticated(finalProfile)
                CoroutineScope(Dispatchers.Default).launch {
                    syncRepository.performInboundSync(userId.toString())
                }
                true
            } else {
                false
            }
        }.getOrElse { false }
    }

    override suspend fun refreshSession(): Boolean {
        return runCatching {
            val accessToken = secureStorage.getToken("access_token")
            if (accessToken.isNullOrBlank()) {
                _sessionState.value = SessionState.Guest
                return false
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

                _sessionState.value = SessionState.Authenticated(finalProfile)
                CoroutineScope(Dispatchers.Default).launch {
                    syncRepository.performInboundSync(userId.toString())
                }
                true
            } else {
                val refreshed = apiClient.refreshToken()
                if (refreshed) {
                    val profile = apiClient.getMe()
                    if (profile != null) {
                        val userId = profile.userId
                        if (!userId.isNullOrBlank()) {
                            secureStorage.saveToken("user_id", userId)
                        }
                        
                        val override = if (!userId.isNullOrBlank()) {
                            localDb.settingsScreenQueries.getUserProfileOverride(userId).executeAsOneOrNull()
                        } else null
                        
                        val finalProfile = if (override != null) {
                            profile.copy(
                                name = override.customName ?: profile.name,
                                profilePicture = if (!override.customProfilePic.isNullOrBlank()) {
                                    ProfileImages(
                                        image150 = override.customProfilePic,
                                        image480 = override.customProfilePic,
                                        image1000 = override.customProfilePic
                                    )
                                } else profile.profilePicture
                            )
                        } else {
                            profile
                        }

                        _sessionState.value = SessionState.Authenticated(finalProfile)
                        CoroutineScope(Dispatchers.Default).launch {
                            syncRepository.performInboundSync(userId.toString())
                        }
                        return true
                    }
                }
                _sessionState.value = SessionState.Guest
                false
            }
        }.getOrElse {
            _sessionState.value = SessionState.Guest
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

    override fun updateUserProfile(
        userId: String,
        name: String,
        bio: String?,
        profilePicUrl: String?,
        coverPhotoUrl: String?
    ) {
        localDb.settingsScreenQueries.transaction {
            localDb.settingsScreenQueries.upsertUserProfileOverride(
                userId = userId,
                customName = name,
                customBio = bio,
                customProfilePic = profilePicUrl,
                customCoverPhoto = coverPhotoUrl
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
