package com.github.hank9999.kook.gateway
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

public interface Gateway : CoroutineScope {
    public val events: SharedFlow<Any>
}