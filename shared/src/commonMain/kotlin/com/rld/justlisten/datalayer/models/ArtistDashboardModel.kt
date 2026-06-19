package com.rld.justlisten.datalayer.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListenCount(
    @SerialName("trackId") val trackId: Int,
    @SerialName("date") val date: String,
    @SerialName("listens") val listens: Int
)

@Serializable
data class MonthlyAggregatePlay(
    @SerialName("totalListens") val totalListens: Int,
    @SerialName("trackIds") val trackIds: List<Int> = emptyList(),
    @SerialName("listenCounts") val listenCounts: List<ListenCount> = emptyList()
)

@Serializable
data class UserTrackListenCountsResponse(
    @SerialName("data") val data: Map<String, MonthlyAggregatePlay> = emptyMap()
)

@Serializable
data class UserTracksDownloadCountResponse(
    @SerialName("data") val data: Long
)

@Serializable
data class SalesAggregate(
    @SerialName("content_type") val contentType: String,
    @SerialName("content_id") val contentId: String,
    @SerialName("purchase_count") val purchaseCount: Int
)

@Serializable
data class SalesAggregateResponse(
    @SerialName("data") val data: List<SalesAggregate> = emptyList()
)
