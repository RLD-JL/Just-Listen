package com.rld.justlisten.viewmodel.artistdashboard

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.repositories.ArtistDashboardRepository
import com.rld.justlisten.datalayer.repositories.AuthRepository
import com.rld.justlisten.datalayer.repositories.SessionState
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.artistdashboard.ArtistDashboardState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlin.time.Instant

class ArtistDashboardViewModel(
    private val artistDashboardRepository: ArtistDashboardRepository,
    private val authRepository: AuthRepository
) : BaseScreenViewModel() {

    private val _state = MutableStateFlow(ArtistDashboardState(isLoading = true))
    val state: StateFlow<ArtistDashboardState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.sessionState.collect { session ->
                if (session is SessionState.Authenticated) {
                    val userId = session.userProfile.userId
                    if (userId != null) {
                        loadDashboardData(userId)
                    } else {
                        _state.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Invalid user account profile"
                            )
                        }
                    }
                } else {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Please log in to view creator metrics"
                        )
                    }
                }
            }
        }
    }

    private fun loadDashboardData(userId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Determine start and end times for the last 6 months using safe epoch math
                val nowSeconds = kotlin.time.Clock.System.now().epochSeconds
                val nowInstant = kotlin.time.Instant.fromEpochSeconds(nowSeconds)
                val endTime = nowInstant.toString()
                
                val sixMonthsAgoSeconds = nowSeconds - 180L * 24 * 60 * 60
                val startTime = kotlin.time.Instant.fromEpochSeconds(sixMonthsAgoSeconds).toString()

                val result = withContext(Dispatchers.IO) {
                    val listens = artistDashboardRepository.getMonthlyListens(userId, startTime, endTime)
                    val downloads = artistDashboardRepository.getDownloadsCount(userId)
                    val sales = artistDashboardRepository.getSalesAggregate(userId)
                    
                    Triple(listens, downloads, sales)
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        monthlyListens = result.first,
                        downloadsCount = result.second,
                        salesAggregate = result.third
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Failed to load dashboard data: ${e.message}"
                    )
                }
            }
        }
    }

    fun handleBack() {
        popBackStack()
    }
}
