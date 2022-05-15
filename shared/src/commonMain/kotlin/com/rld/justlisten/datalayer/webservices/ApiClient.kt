package com.rld.justlisten.datalayer.webservices

import com.rld.justlisten.datalayer.utils.Constants.BASEURL
import com.rld.justlisten.datalayer.utils.Constants.bestResponseTime
import com.rld.justlisten.datalayer.utils.Constants.listOfBaseUrls
import com.rld.justlisten.datalayer.utils.Constants.usedBasedUrl
import com.rld.justlisten.datalayer.webservices.apis.playlistcalls.PlayListResponse
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import kotlin.random.Random

class ApiClient {

    val baseUrl = BASEURL

    val client = HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json {
                ignoreUnknownKeys = true

            })
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
        }
    }


    suspend inline fun <reified T : Any> getResponse(endpoint: String): T? {
        var numberOfCalls = 0
        var wasSuccessful = false
        while (numberOfCalls < 10 && !wasSuccessful) {
            val baseUrl = usedBasedUrl["goodBaseUrl"] ?: listOfBaseUrls[Random.nextInt(0, listOfBaseUrls.size)]
            val url = "${baseUrl}/v1$endpoint"
            try {
                val firstTime = Clock.System.now().nanosecondsOfSecond
                val response = client.get<T>(url)
                when (response) {
                    is PlayListResponse -> wasSuccessful = response.error.isNullOrEmpty()
                }
                if (wasSuccessful){
                    val secondTime = Clock.System.now().nanosecondsOfSecond
                    val responseTime = secondTime - firstTime
                    val savedBestResponseTime = bestResponseTime["goodBaseUrl"] ?: Int.MAX_VALUE
                    bestResponseTime["goodBaseUrl"] = if (savedBestResponseTime > responseTime) responseTime else savedBestResponseTime
                    usedBasedUrl["goodBaseUrl"] = if (bestResponseTime["goodBaseUrl"] == responseTime) baseUrl else usedBasedUrl["goodBaseUrl"] ?: baseUrl
                    return  response }
            } catch (e: Exception) {
                numberOfCalls +=1
                println("Something went wrong: $url" + e.cause + " message= " + e.message)
            }
        }
        return null
    }
}

