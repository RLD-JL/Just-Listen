package com.rld.justlisten.datalayer.webservices

import com.rld.justlisten.datalayer.utils.Constants.listOfBaseUrls
import kotlinx.datetime.Clock
import kotlin.random.Random

class DiscoveryNodeService {
    private var bestNode: String? = null
    private var bestResponseTime: Long = Long.MAX_VALUE

    fun getBestNode(): String {
        return bestNode ?: listOfBaseUrls[Random(Clock.System.now().toEpochMilliseconds()).nextInt(listOfBaseUrls.size)]
    }

    fun updateNodePerformance(node: String, responseTime: Long) {
        if (responseTime < bestResponseTime) {
            bestResponseTime = responseTime
            bestNode = node
        }
    }
}
