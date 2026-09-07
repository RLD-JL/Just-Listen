package com.rld.justlisten.datalayer.webservices

import com.rld.justlisten.datalayer.utils.Constants
import com.rld.justlisten.util.SecureStorage
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import io.ktor.client.engine.HttpClientEngine
import io.ktor.util.AttributeKey
import com.rld.justlisten.util.authSessionLock

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String
)

class ApiRequestException(
    val statusCode: Int,
    val isTransient: Boolean,
    message: String,
) : Exception(message)

@PublishedApi
internal enum class TokenRefreshResult {
    Success,
    Rejected,
    Unavailable,
}

open class ApiClient(
    val apiKey: String = "",
    val secureStorage: SecureStorage,
    httpClientEngine: HttpClientEngine? = null,
) {
    private val tokenMutex = Mutex()

    private val configureClient: HttpClientConfig<*>.() -> Unit = {
        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 10000
            socketTimeoutMillis = 15000
            requestTimeoutMillis = 30000
        }
        install(io.ktor.client.plugins.logging.Logging) {
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    co.touchlab.kermit.Logger.d { "KtorClient: $message" }
                }
            }
            level = io.ktor.client.plugins.logging.LogLevel.HEADERS
        }
        install(HttpRequestRetry) {
            maxRetries = 3
            retryIf { _, response -> response.status.value in 500..599 }
            delayMillis { retry -> retry * 1000L }
        }
        defaultRequest {
            if (apiKey.isNotBlank()) {
                header("X-API-KEY", apiKey)
            }
            val accessToken = if (attributes.contains(SYNC_CREDENTIALS)) null else secureStorage.getToken("access_token")
            if (!accessToken.isNullOrBlank()) {
                header("Authorization", "Bearer $accessToken")
            }
            val userId = if (attributes.contains(SYNC_CREDENTIALS)) null else secureStorage.getToken("user_id")
            if (!userId.isNullOrBlank() &&
                !url.encodedPath.endsWith("/oauth/token") &&
                url.parameters["user_id"].isNullOrBlank()
            ) {
                url.parameters.append("user_id", userId)
            }
        }
    }

    val client = (httpClientEngine?.let { HttpClient(it, configureClient) }
        ?: HttpClient(configureClient)).apply {
        sendPipeline.intercept(io.ktor.client.request.HttpSendPipeline.State) {
            if (context.url.encodedPath.contains("/unsplash")) {
                context.headers.remove("Authorization")
                context.headers.remove("X-API-KEY")
                context.url.parameters.remove("user_id")
            }
        }
    }


    @PublishedApi
    internal suspend fun syncRequestCredentials(): SyncRequestCredentials? {
        val session = currentCoroutineContext()[SyncSession] ?: return null
        return authSessionLock.withLock {
            session.requireActive(secureStorage)
            SyncRequestCredentials(session.userId, secureStorage.getToken("access_token")!!)
        }
    }

    @PublishedApi
    internal suspend fun refreshTokenResult(failedToken: String? = null): TokenRefreshResult {
        val syncSession = currentCoroutineContext()[SyncSession]
        return tokenMutex.withLock {
            val (refreshIdentity, currentToken, refreshToken) = authSessionLock.withLock {
                syncSession?.requireActive(secureStorage)
                Triple(
                    secureStorage.getToken("auth_session_id") to secureStorage.getToken("user_id"),
                    secureStorage.getToken("access_token"),
                    secureStorage.getToken("refresh_token"),
                )
            }
            if (failedToken != null && !currentToken.isNullOrBlank() && currentToken != failedToken) {
                return TokenRefreshResult.Success
            }
            if (refreshToken == null) return TokenRefreshResult.Rejected
            val url = "${Constants.BASEURL}/v1/oauth/token"
            try {
                val response = client.post(url) {
                    attributes.put(SYNC_CREDENTIALS, true)
                    // Avoid infinite loops by overriding the bearer token for this request
                    header("Authorization", "")
                    contentType(ContentType.Application.FormUrlEncoded)
                    setBody(
                        "grant_type=refresh_token" +
                        "&refresh_token=$refreshToken" +
                        "&client_id=$apiKey"
                    )
                }
                if (response.status.isSuccess()) {
                    val tokenResponse = response.body<TokenResponse>()
                    authSessionLock.withLock {
                        if (refreshIdentity != (secureStorage.getToken("auth_session_id") to secureStorage.getToken("user_id"))) {
                            throw SyncSessionChanged()
                        }
                        syncSession?.requireActive(secureStorage)
                        secureStorage.saveToken("access_token", tokenResponse.accessToken)
                        secureStorage.saveToken("refresh_token", tokenResponse.refreshToken)
                    }
                    TokenRefreshResult.Success
                } else if (response.status.value in 400..499 &&
                    response.status != HttpStatusCode.RequestTimeout &&
                    response.status != HttpStatusCode.TooManyRequests
                ) {
                    TokenRefreshResult.Rejected
                } else {
                    TokenRefreshResult.Unavailable
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                Logger.e(e) { "Error refreshing token" }
                TokenRefreshResult.Unavailable
            }
        }
    }

    suspend fun refreshToken(failedToken: String? = null): Boolean =
        refreshTokenResult(failedToken) == TokenRefreshResult.Success

    suspend inline fun <reified T : Any> getResponse(endpoint: String): T? {
        val url = "${Constants.BASEURL}/v1$endpoint"
        Logger.d { "ApiClient: GET request to: $url" }
        return try {
            var credentials = syncRequestCredentials()
            val tokenBeforeRequest = credentials?.token ?: secureStorage.getToken("access_token")
            var response = client.get(url) { applySyncCredentials(credentials) }
            if (response.status == HttpStatusCode.Unauthorized) {
                when (refreshTokenResult(tokenBeforeRequest)) {
                    TokenRefreshResult.Success -> {
                        credentials = syncRequestCredentials()
                        response = client.get(url) { applySyncCredentials(credentials) }
                    }
                    TokenRefreshResult.Unavailable -> throw ApiRequestException(
                        statusCode = response.status.value,
                        isTransient = true,
                        message = "Authentication could not be refreshed while the service is unavailable",
                    )
                    TokenRefreshResult.Rejected -> Unit
                }
            }
            if (response.status.isSuccess()) {
                response.body<T>()
            } else {
                val statusCode = response.status.value
                throw ApiRequestException(
                    statusCode = statusCode,
                    isTransient = statusCode >= 500 ||
                        response.status == HttpStatusCode.RequestTimeout ||
                        response.status == HttpStatusCode.TooManyRequests,
                    message = "HTTP error $statusCode fetching $url",
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e(e) { "Error fetching $url" }
            throw e
        }
    }

    suspend inline fun <reified T : Any> postResponse(endpoint: String): T? =
        postResponse<T, String>(endpoint, null)

    suspend inline fun <reified T : Any, reified B : Any> postResponse(endpoint: String, body: B?): T? {
        val url = "${Constants.BASEURL}/v1$endpoint"
        Logger.d { "ApiClient: POST request to: $url" }
        return try {
            var credentials = syncRequestCredentials()
            val tokenBeforeRequest = credentials?.token ?: secureStorage.getToken("access_token")
            var response = client.post(url) {
                applySyncCredentials(credentials)
                if (body != null) {
                    if (body is String) {
                        contentType(ContentType.Application.FormUrlEncoded)
                    } else {
                        contentType(ContentType.Application.Json)
                    }
                    setBody(body)
                }
            }
            if (response.status == HttpStatusCode.Unauthorized) {
                val refreshed = refreshToken(tokenBeforeRequest)
                if (refreshed) {
                    credentials = syncRequestCredentials()
                    response = client.post(url) {
                        applySyncCredentials(credentials)
                        if (body != null) {
                            if (body is String) {
                                contentType(ContentType.Application.FormUrlEncoded)
                            } else {
                                contentType(ContentType.Application.Json)
                            }
                            setBody(body)
                        }
                    }
                }
            }
            if (response.status.isSuccess()) {
                response.body<T>()
            } else {
                val statusCode = response.status.value
                throw ApiRequestException(
                    statusCode = statusCode,
                    isTransient = statusCode >= 500 ||
                        response.status == HttpStatusCode.RequestTimeout ||
                        response.status == HttpStatusCode.TooManyRequests,
                    message = "HTTP error $statusCode posting to $url",
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e(e) { "Error posting to $url" }
            throw e
        }
    }

    suspend inline fun <reified T : Any> putResponse(endpoint: String, body: Any? = null): T? {
        val url = "${Constants.BASEURL}/v1$endpoint"
        Logger.d { "ApiClient: PUT request to: $url" }
        return try {
            var credentials = syncRequestCredentials()
            val tokenBeforeRequest = credentials?.token ?: secureStorage.getToken("access_token")
            var response = client.put(url) {
                applySyncCredentials(credentials)
                if (body != null) {
                    if (body is String) {
                        contentType(ContentType.Application.FormUrlEncoded)
                    } else {
                        contentType(ContentType.Application.Json)
                    }
                    setBody(body)
                }
            }
            if (response.status == HttpStatusCode.Unauthorized) {
                val refreshed = refreshToken(tokenBeforeRequest)
                if (refreshed) {
                    credentials = syncRequestCredentials()
                    response = client.put(url) {
                        applySyncCredentials(credentials)
                        if (body != null) {
                            if (body is String) {
                                contentType(ContentType.Application.FormUrlEncoded)
                            } else {
                                contentType(ContentType.Application.Json)
                            }
                            setBody(body)
                        }
                    }
                }
            }
            if (response.status.isSuccess()) {
                response.body<T>()
            } else {
                throw Exception("HTTP error ${response.status.value} putting to $url")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e(e) { "Error putting to $url" }
            throw e
        }
    }

    suspend inline fun <reified T : Any> deleteResponse(endpoint: String): T? =
        deleteResponse<T, String>(endpoint, null)

    suspend inline fun <reified T : Any, reified B : Any> deleteResponse(
        endpoint: String,
        body: B?,
    ): T? {
        val url = "${Constants.BASEURL}/v1$endpoint"
        return try {
            var credentials = syncRequestCredentials()
            val tokenBeforeRequest = credentials?.token ?: secureStorage.getToken("access_token")
            var response = client.delete(url) {
                applySyncCredentials(credentials)
                if (body != null) {
                    if (body is String) {
                        contentType(ContentType.Application.FormUrlEncoded)
                    } else {
                        contentType(ContentType.Application.Json)
                    }
                    setBody(body)
                }
            }
            if (response.status == HttpStatusCode.Unauthorized) {
                val refreshed = refreshToken(tokenBeforeRequest)
                if (refreshed) {
                    credentials = syncRequestCredentials()
                    response = client.delete(url) {
                        applySyncCredentials(credentials)
                        if (body != null) {
                            if (body is String) {
                                contentType(ContentType.Application.FormUrlEncoded)
                            } else {
                                contentType(ContentType.Application.Json)
                            }
                            setBody(body)
                        }
                    }
                }
            }
            if (response.status.isSuccess()) {
                response.body<T>()
            } else {
                val statusCode = response.status.value
                throw ApiRequestException(
                    statusCode = statusCode,
                    isTransient = statusCode >= 500 ||
                        response.status == HttpStatusCode.RequestTimeout ||
                        response.status == HttpStatusCode.TooManyRequests,
                    message = "HTTP error $statusCode deleting from $url",
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e(e) { "Error deleting from $url" }
            throw e
        }
    }
}

@PublishedApi
internal data class SyncRequestCredentials(val userId: String, val token: String)

private val SYNC_CREDENTIALS = AttributeKey<Boolean>("sync-credentials")

@PublishedApi
internal fun HttpRequestBuilder.applySyncCredentials(credentials: SyncRequestCredentials?) {
    if (credentials == null) return
    attributes.put(SYNC_CREDENTIALS, true)
    headers.remove("Authorization")
    header("Authorization", "Bearer ${credentials.token}")
    url.parameters["user_id"] = credentials.userId
}
