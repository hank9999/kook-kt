package com.github.hank9999.kook.gateway

import io.ktor.server.engine.ApplicationEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.coroutines.CoroutineContext

class WebHookGateway : IGateway {
    constructor(engine: ApplicationEngine, dispatcher: CoroutineContext, events: MutableSharedFlow<Event>) {
        this.engine = engine
        this.coroutineContext = SupervisorJob() + dispatcher
        this.events = events
    }

    private val engine: ApplicationEngine
    override val coroutineContext: CoroutineContext
    override val events: SharedFlow<Event>
}