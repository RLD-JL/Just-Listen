package com.rld.justlisten.media

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/** Serializes controls, including controls issued while the player connects. */
internal class ReadyCommandQueue<T>(
    scope: CoroutineScope,
    private val acquire: suspend () -> T,
    private val onFailure: (Exception) -> Unit,
) {
    private val commands = Channel<(T) -> Unit>(Channel.UNLIMITED)

    init {
        scope.launch {
            for (command in commands) {
                try {
                    command(acquire())
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    onFailure(e)
                }
            }
        }.invokeOnCompletion { commands.cancel() }
    }

    fun submit(command: (T) -> Unit) {
        commands.trySend(command)
    }
}
