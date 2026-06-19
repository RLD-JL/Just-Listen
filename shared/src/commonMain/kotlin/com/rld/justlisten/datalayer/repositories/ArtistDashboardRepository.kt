package com.rld.justlisten.datalayer.repositories

import com.rld.justlisten.datalayer.models.MonthlyAggregatePlay
import com.rld.justlisten.datalayer.models.SalesAggregate
import com.rld.justlisten.datalayer.models.UserTrackListenCountsResponse
import com.rld.justlisten.datalayer.models.UserTracksDownloadCountResponse
import com.rld.justlisten.datalayer.models.SalesAggregateResponse
import com.rld.justlisten.datalayer.webservices.ApiClient

interface ArtistDashboardRepository {
    suspend fun getMonthlyListens(userId: String, startTime: String, endTime: String): Map<String, MonthlyAggregatePlay>
    suspend fun getDownloadsCount(userId: String): Long
    suspend fun getSalesAggregate(userId: String): List<SalesAggregate>
}

class ArtistDashboardRepositoryImpl(private val apiClient: ApiClient) : ArtistDashboardRepository {
    override suspend fun getMonthlyListens(userId: String, startTime: String, endTime: String): Map<String, MonthlyAggregatePlay> {
        val response: UserTrackListenCountsResponse? = apiClient.getResponse("/users/$userId/listen_counts_monthly?start_time=$startTime&end_time=$endTime")
        return response?.data ?: emptyMap()
    }

    override suspend fun getDownloadsCount(userId: String): Long {
        val response: UserTracksDownloadCountResponse? = apiClient.getResponse("/users/$userId/tracks/download_count")
        return response?.data ?: 0L
    }

    override suspend fun getSalesAggregate(userId: String): List<SalesAggregate> {
        val response: SalesAggregateResponse? = apiClient.getResponse("/users/$userId/sales/aggregate")
        return response?.data ?: emptyList()
    }
}
