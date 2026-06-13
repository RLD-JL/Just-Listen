package com.rld.justlisten.datalayer.webservices.apis.unsplashcalls

import com.rld.justlisten.datalayer.utils.Constants
import com.rld.justlisten.datalayer.webservices.ApiClient
import io.ktor.client.call.*
import io.ktor.client.plugins.timeout
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

@Serializable
data class UnsplashPhotoUrls(
    @SerialName("raw") val raw: String,
    @SerialName("full") val full: String,
    @SerialName("regular") val regular: String,
    @SerialName("small") val small: String,
    @SerialName("thumb") val thumb: String
)

@Serializable
data class UnsplashPhoto(
    @SerialName("id") val id: String,
    @SerialName("urls") val urls: UnsplashPhotoUrls,
    @SerialName("description") val description: String? = null
)

@Serializable
data class UnsplashSearchResponse(
    @SerialName("results") val results: List<UnsplashPhoto>
)

suspend fun ApiClient.getUnsplashPhotos(
    query: String,
    page: Int = 1,
    perPage: Int = 18
): List<UnsplashPhoto>? {
    val url = "${Constants.BASEURL}/unsplash/search/photos?query=$query&page=$page&per_page=$perPage"
    co.touchlab.kermit.Logger.d { "UnsplashPresetApi: Starting GET request to URL: $url" }
    return try {
        val response = client.get(url) {
            header("Origin", "https://audius.co")
            header("Referer", "https://audius.co/")
            timeout {
                socketTimeoutMillis = 30000
                requestTimeoutMillis = 40000
            }
        }
        co.touchlab.kermit.Logger.d { "UnsplashPresetApi: Received response with status: ${response.status}" }
        if (response.status.value in 200..299) {
            val bodyString = response.bodyAsText()
            co.touchlab.kermit.Logger.d { "UnsplashPresetApi: Body snippet (first 500 chars): ${bodyString.take(500)}" }
            val searchResponse = json.decodeFromString<UnsplashSearchResponse>(bodyString)
            searchResponse.results
        } else {
            co.touchlab.kermit.Logger.w { "UnsplashPresetApi: Non-success status: ${response.status.value}" }
            null
        }
    } catch (e: Exception) {
        co.touchlab.kermit.Logger.e(e) { "UnsplashPresetApi: Exception occurred for $url" }
        null
    }
}
