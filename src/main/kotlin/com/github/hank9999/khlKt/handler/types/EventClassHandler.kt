package com.github.hank9999.khlKt.handler.types

import kotlin.reflect.KFunction

data class EventClassHandler(
    val classInstance: Any,
    val function: KFunction<*>
)
