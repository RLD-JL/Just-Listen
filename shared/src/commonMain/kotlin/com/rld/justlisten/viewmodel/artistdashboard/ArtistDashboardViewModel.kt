package com.rld.justlisten.viewmodel.artistdashboard

import androidx.lifecycle.viewModelScope
import com.rld.justlisten.datalayer.repositories.ArtistDashboardRepository
import com.rld.justlisten.datalayer.repositories.AuthRepository
import com.rld.justlisten.datalayer.repositories.SessionState
import com.rld.justlisten.datalayer.webservices.ApiRequestException
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.screens.artistdashboard.ArtistDashboardState
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ArtistDashboardViewModel(
    private val artistDashboardRepository: ArtistDashboardRepository,
    private val authRepository: AuthRepository
) : BaseScreenViewModel() {
    private val _state = MutableStateFlow(ArtistDashboardState())
    val state: StateFlow<ArtistDashboardState> = _state.asStateFlow()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            authRepository.sessionState.collect { load(it) }
        }
    }

    fun retry() = load(authRepository.sessionState.value)

    private fun load(session: SessionState) {
        loadJob?.cancel()
        val userId = (session as? SessionState.Authenticated)?.userProfile?.userId
        if (userId.isNullOrBlank()) {
            // Do not retain another session's creator metrics.
            _state.value = ArtistDashboardState(isLoading = false,
                errorMessage = "Please log in to view creator metrics")
            return
        }
        _state.value = ArtistDashboardState(isLoading = false,
            listensLoading = true, downloadsLoading = true, salesLoading = true)
        loadJob = viewModelScope.launch {
            val now = kotlin.time.Clock.System.now().epochSeconds
            val end = kotlin.time.Instant.fromEpochSeconds(now).toString()
            val start = kotlin.time.Instant.fromEpochSeconds(now - 180L * 24 * 60 * 60).toString()
            // Publish each result independently; unavailable does not mean zero.
            launch {
                val result = metric { artistDashboardRepository.getMonthlyListens(userId, start, end) }
                ensureActive()
                if (authRepository.sessionState.value == session) _state.update { it.copy(
                    listensLoading = false, monthlyListens = result.getOrDefault(emptyMap()),
                    listensError = result.exceptionOrNull()?.messageFor("Listen statistics")) }
            }
            launch {
                val result = metric { artistDashboardRepository.getDownloadsCount(userId) }
                ensureActive()
                if (authRepository.sessionState.value == session) _state.update { it.copy(
                    downloadsLoading = false, downloadsCount = result.getOrDefault(0L),
                    downloadsError = result.exceptionOrNull()?.messageFor("Download statistics")) }
            }
            launch {
                val result = metric { artistDashboardRepository.getSalesAggregate(userId) }
                ensureActive()
                if (authRepository.sessionState.value == session) _state.update { it.copy(
                    salesLoading = false, salesAggregate = result.getOrDefault(emptyList()),
                    salesError = result.exceptionOrNull()?.messageFor("Sales statistics")) }
            }
        }
    }

    private suspend fun <T> metric(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun Throwable.messageFor(section: String): String =
        if (this is ApiRequestException && statusCode == 403)
            "$section unavailable: Audius denied access."
        else "$section unavailable. Please try again."

    fun handleBack() = popBackStack()
}
