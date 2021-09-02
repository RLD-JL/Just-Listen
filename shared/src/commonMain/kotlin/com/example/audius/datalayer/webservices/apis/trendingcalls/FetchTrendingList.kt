package com.example.audius.datalayer.webservices.apis.trendingcalls

import com.example.audius.datalayer.models.TrendingListModel
import com.example.audius.datalayer.webservices.ApiClient
import com.example.audius.viewmodel.screens.trending.TrendingListItem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

suspend fun ApiClient.fetchTrendingList(): TrendingListResponse? {
    return getResponse("/tracks/trending?app_name=EXAMPLEAPP")
}


@Serializable
data class TrendingListResponse(
    @SerialName("data") val data : List<TrendingListModel>,
    @SerialName("err") val error : String? = null,
)