package com.rld.justlisten.viewmodel.screens.artistdashboard

import com.rld.justlisten.datalayer.models.MonthlyAggregatePlay
import com.rld.justlisten.datalayer.models.SalesAggregate

data class ArtistDashboardState(
    val isLoading: Boolean = true,
    val downloadsCount: Long = 0L,
    val monthlyListens: Map<String, MonthlyAggregatePlay> = emptyMap(),
    val salesAggregate: List<SalesAggregate> = emptyList(),
    val errorMessage: String? = null
)
