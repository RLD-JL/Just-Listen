package com.rld.justlisten.datalayer.webservices

import com.rld.justlisten.datalayer.utils.Constants.bestResponseTime
import com.rld.justlisten.datalayer.utils.Constants.listOfBaseUrls
import com.rld.justlisten.datalayer.utils.Constants.usedBasedUrl
import com.rld.justlisten.datalayer.webservices.apis.playlistcalls.PlayListResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlin.collections.set
import kotlin.random.Random

class ApiClient {

    val client = HttpClient {
        engine {
            // this: HttpClientEngineConfig
            threadsCount = 4
        }

        install(ContentNegotiation) {
            json(Json {
                isLenient = true
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 3000
            socketTimeoutMillis = 4500
            requestTimeoutMillis = 10000
        }
    }


    suspend inline fun <reified T : Any> getResponse(endpoint: String): T? {
        var numberOfCalls = 0
        var wasSuccessful = false
        while (numberOfCalls < 15 && !wasSuccessful) {
            val random =
                Random(Clock.System.now().toEpochMilliseconds()).nextInt(0, listOfBaseUrls.size)
            val baseUrl = usedBasedUrl["goodBaseUrl"] ?: listOfBaseUrls[random]
            val url = "${baseUrl}/v1$endpoint"
            try {
                val response = client.get(url)
                val body = response.body<T>()
                when (body) {
                    is PlayListResponse -> wasSuccessful = body.error.isNullOrEmpty()
                }
                if (wasSuccessful) {
                    val responseTime =
                        response.responseTime.timestamp - response.requestTime.timestamp
                    val savedBestResponseTime = bestResponseTime["goodBaseUrl"] ?: Long.MAX_VALUE
                    bestResponseTime["goodBaseUrl"] =
                        if (savedBestResponseTime > responseTime) responseTime else savedBestResponseTime
                    usedBasedUrl["goodBaseUrl"] =
                        if (bestResponseTime["goodBaseUrl"] == responseTime) baseUrl else usedBasedUrl["goodBaseUrl"]
                            ?: baseUrl
                    return body
                }
            } catch (e: Exception) {
                numberOfCalls += 1
            }
        }
        return null
    }
}

