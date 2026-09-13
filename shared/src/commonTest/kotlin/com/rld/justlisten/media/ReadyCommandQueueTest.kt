package com.rld.justlisten.media

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ReadyCommandQueueTest {
    @Test
    fun shuffleAndPlayWaitForConnectionAndKeepTheirOrder() = runTest {
        val ready = CompletableDeferred<MutableList<String>>()
        val player = mutableListOf<String>()
        val queue = ReadyCommandQueue(backgroundScope, { ready.await() }, { throw it })
        queue.submit { it.add("shuffle") }
        queue.submit { it.add("load") }
        queue.submit { it.add("play") }
        runCurrent()
        assertTrue(player.isEmpty())
        ready.complete(player)
        runCurrent()
        assertEquals(listOf("shuffle", "load", "play"), player)
    }

    @Test
    fun failedConnectionDoesNotDisableLaterCommands() = runTest {
        var attempts = 0
        val failures = mutableListOf<Exception>()
        val player = mutableListOf<String>()
        val queue = ReadyCommandQueue(backgroundScope, {
            if (attempts++ == 0) error("Disconnected")
            player
        }, failures::add)
        queue.submit { it.add("first") }
        queue.submit { it.add("retry") }
        runCurrent()
        assertEquals(1, failures.size)
        assertEquals(listOf("retry"), player)
    }
}
