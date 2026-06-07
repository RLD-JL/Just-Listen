package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.apis.authcalls.MeResponse
import com.rld.justlisten.datalayer.webservices.apis.authcalls.exchangeCodeForTokens
import com.rld.justlisten.datalayer.webservices.apis.authcalls.getMe
import com.rld.justlisten.util.PkceCrypto
import com.rld.justlisten.util.SecureStorage
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
}

class AuthRepositoryImpl(
    private val apiClient: ApiClient,
    private val secureStorage: SecureStorage,
    private val pkceCrypto: PkceCrypto,
    private val syncRepository: SyncRepository,
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
        val verifier = currentVerifier ?: secureStorage.getToken("code_verifier") ?: return false
        val tokenResponse = apiClient.exchangeCodeForTokens(code, verifier, redirectUri) ?: return false
        secureStorage.saveToken("access_token", tokenResponse.accessToken)
        secureStorage.saveToken("refresh_token", tokenResponse.refreshToken)
        
        // Fetch user profile
        val userProfile = apiClient.getMe()
        return if (userProfile != null) {
            if (!userProfile.userId.isNullOrBlank()) {
                secureStorage.saveToken("user_id", userProfile.userId)
            }
            _sessionState.value = SessionState.Authenticated(userProfile)
            CoroutineScope(Dispatchers.Default).launch {
                syncRepository.performInboundSync(userProfile.userId.toString())
            }
            true
        } else {
            false
        }
    }

    override suspend fun refreshSession(): Boolean {
        val accessToken = secureStorage.getToken("access_token")
        if (accessToken.isNullOrBlank()) {
            _sessionState.value = SessionState.Guest
            return false
        }
        val userProfile = apiClient.getMe()
        return if (userProfile != null) {
            if (!userProfile.userId.isNullOrBlank()) {
                secureStorage.saveToken("user_id", userProfile.userId)
            }
            _sessionState.value = SessionState.Authenticated(userProfile)
            CoroutineScope(Dispatchers.Default).launch {
                syncRepository.performInboundSync(userProfile.userId.toString())
            }
            true
        } else {
            val refreshed = apiClient.refreshToken()
            if (refreshed) {
                val profile = apiClient.getMe()
                if (profile != null) {
                    if (!profile.userId.isNullOrBlank()) {
                        secureStorage.saveToken("user_id", profile.userId)
                    }
                    _sessionState.value = SessionState.Authenticated(profile)
                    CoroutineScope(Dispatchers.Default).launch {
                        syncRepository.performInboundSync(profile.userId.toString())
                    }
                    return true
                }
            }
            _sessionState.value = SessionState.Guest
            false
        }
    }

    override fun logout() {
        secureStorage.clear()
        _sessionState.value = SessionState.Guest
    }
}
