package com.github.hank9999.kook.gateway
import com.github.hank9999.kook.gateway.entity.Event
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.time.Duration

public interface IGateway : CoroutineScope {
    val events: SharedFlow<Event>
    val ping: StateFlow<Duration?>

    suspend fun start()

    suspend fun stop()
}
