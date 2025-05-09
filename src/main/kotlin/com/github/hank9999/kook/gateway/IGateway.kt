package com.github.hank9999.kook.gateway
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.SharedFlow

public interface IGateway : CoroutineScope {
    val events: SharedFlow<Event>
}