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

@Serializable
data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String
)

open class ApiClient(
    val apiKey: String = "",
    val secureStorage: SecureStorage
) {
    private val tokenMutex = Mutex()

    val client = HttpClient {
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
            val accessToken = secureStorage.getToken("access_token")
            if (!accessToken.isNullOrBlank()) {
                header("Authorization", "Bearer $accessToken")
            }
            val userId = secureStorage.getToken("user_id")
            if (!userId.isNullOrBlank() && !url.encodedPath.endsWith("/oauth/token")) {
                url.parameters.append("user_id", userId)
            }
        }
    }.apply {
        sendPipeline.intercept(io.ktor.client.request.HttpSendPipeline.State) {
            if (context.url.encodedPath.contains("/unsplash")) {
                context.headers.remove("Authorization")
                context.headers.remove("X-API-KEY")
                context.url.parameters.remove("user_id")
            }
        }
    }

    suspend fun refreshToken(failedToken: String? = null): Boolean {
        return tokenMutex.withLock {
            val currentToken = secureStorage.getToken("access_token")
            if (!currentToken.isNullOrBlank() && currentToken != failedToken) {
                return true
            }
            val refreshToken = secureStorage.getToken("refresh_token") ?: return false
            val url = "${Constants.BASEURL}/v1/oauth/token"
            try {
                val response = client.post(url) {
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
                    secureStorage.saveToken("access_token", tokenResponse.accessToken)
                    secureStorage.saveToken("refresh_token", tokenResponse.refreshToken)
                    true
                } else {
                    false
                }
            } catch (e: Exception) {
                Logger.e(e) { "Error refreshing token" }
                false
            }
        }
    }

    suspend inline fun <reified T : Any> getResponse(endpoint: String): T? {
        val url = "${Constants.BASEURL}/v1$endpoint"
        Logger.d { "ApiClient: GET request to: $url" }
        return try {
            val tokenBeforeRequest = secureStorage.getToken("access_token")
            var response = client.get(url)
            if (response.status == HttpStatusCode.Unauthorized) {
                val refreshed = refreshToken(tokenBeforeRequest)
                if (refreshed) {
                    response = client.get(url)
                }
            }
            if (response.status.isSuccess()) {
                response.body<T>()
            } else {
                throw Exception("HTTP error ${response.status.value} fetching $url")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e(e) { "Error fetching $url" }
            throw e
        }
    }

    suspend inline fun <reified T : Any> postResponse(endpoint: String, body: Any? = null): T? {
        val url = "${Constants.BASEURL}/v1$endpoint"
        Logger.d { "ApiClient: POST request to: $url" }
        return try {
            val tokenBeforeRequest = secureStorage.getToken("access_token")
            var response = client.post(url) {
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
                    response = client.post(url) {
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
                throw Exception("HTTP error ${response.status.value} posting to $url")
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
            val tokenBeforeRequest = secureStorage.getToken("access_token")
            var response = client.put(url) {
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
                    response = client.put(url) {
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

    suspend inline fun <reified T : Any> deleteResponse(endpoint: String): T? {
        val url = "${Constants.BASEURL}/v1$endpoint"
        return try {
            val tokenBeforeRequest = secureStorage.getToken("access_token")
            var response = client.delete(url)
            if (response.status == HttpStatusCode.Unauthorized) {
                val refreshed = refreshToken(tokenBeforeRequest)
                if (refreshed) {
                    response = client.delete(url)
                }
            }
            if (response.status.isSuccess()) {
                response.body<T>()
            } else {
                throw Exception("HTTP error ${response.status.value} deleting from $url")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Logger.e(e) { "Error deleting from $url" }
            throw e
        }
    }
}