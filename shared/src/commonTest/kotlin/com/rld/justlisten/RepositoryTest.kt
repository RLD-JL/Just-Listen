package com.rld.justlisten

import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.webservices.ApiClient
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RepositoryTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun testFavoriteEvents() = runTest {
        // Create repository with null LocalDb (only safe for methods not using it)
        val repository = Repository(null, ApiClient(), false)
        
        val collectedEvents = mutableListOf<Pair<String, Boolean>>()
        
        // Use UnconfinedTestDispatcher to start collecting immediately
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            repository.favoriteEvents.collect {
                collectedEvents.add(it)
            }
        }
        
        // Emit events
        repository.emitFavoriteEvent("song1", true)
        repository.emitFavoriteEvent("song2", false)
        
        // Assert
        assertEquals(2, collectedEvents.size)
        assertEquals("song1", collectedEvents[0].first)
        assertEquals(true, collectedEvents[0].second)
        assertEquals("song2", collectedEvents[1].first)
        assertEquals(false, collectedEvents[1].second)
        
        job.cancel()
    }
}
