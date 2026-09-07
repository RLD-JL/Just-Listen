package com.rld.justlisten.viewmodel.player

import androidx.lifecycle.ViewModelStore
import com.rld.justlisten.datalayer.models.*
import com.rld.justlisten.datalayer.repositories.*
import com.rld.justlisten.datalayer.webservices.ApiRequestException
import com.rld.justlisten.datalayer.webservices.apis.authcalls.MeResponse
import com.rld.justlisten.viewmodel.artistdashboard.ArtistDashboardViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistDashboardViewModelTest {
    private class Metrics : ArtistDashboardRepository {
        var salesFailure = true
        var salesGate: CompletableDeferred<Unit>? = null
        override suspend fun getMonthlyListens(userId: String, startTime: String, endTime: String) =
            mapOf("2026-09" to MonthlyAggregatePlay(totalListens = 42))
        override suspend fun getDownloadsCount(userId: String) = 7L
        override suspend fun getSalesAggregate(userId: String): List<SalesAggregate> {
            salesGate?.await()
            if (salesFailure) throw ApiRequestException(403, false, "internal URL")
            return emptyList()
        }
    }

    @Test
    fun forbiddenSalesPreservesMetricsAndRetryCanReturnRealZero() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val repo = Metrics()
            val auth = authenticated()
            val vm = ArtistDashboardViewModel(repo, auth)
            store.put("dashboard", vm)
            advanceUntilIdle()
            assertNull(vm.state.value.errorMessage)
            assertEquals(42, vm.state.value.monthlyListens.values.single().totalListens)
            assertEquals(7L, vm.state.value.downloadsCount)
            assertEquals("Sales statistics unavailable: Audius denied access.", vm.state.value.salesError)
            repo.salesFailure = false
            vm.retry()
            advanceUntilIdle()
            assertNull(vm.state.value.salesError)
            assertFalse(vm.state.value.salesLoading)
            assertTrue(vm.state.value.salesAggregate.isEmpty())
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    @Test
    fun slowSalesDoesNotBlockOtherSectionsAndLogoutClearsMetrics() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val gate = CompletableDeferred<Unit>()
            val repo = Metrics().apply { salesGate = gate }
            val auth = authenticated()
            val vm = ArtistDashboardViewModel(repo, auth)
            store.put("dashboard", vm)
            runCurrent()
            assertFalse(vm.state.value.downloadsLoading)
            assertEquals(7L, vm.state.value.downloadsCount)
            assertTrue(vm.state.value.salesLoading)
            auth.setSessionState(SessionState.Guest)
            runCurrent()
            gate.complete(Unit)
            advanceUntilIdle()
            assertTrue(vm.state.value.monthlyListens.isEmpty())
            assertEquals(0L, vm.state.value.downloadsCount)
            assertNotNull(vm.state.value.errorMessage)
            assertNull(vm.state.value.salesError)
        } finally { store.clear(); Dispatchers.resetMain() }
    }

    private fun authenticated() = FakeAuthRepository().apply {
        setSessionState(SessionState.Authenticated(MeResponse(userId = "A", name = "Artist",
            handle = "artist", verified = false, profilePicture = null)))
    }
}
