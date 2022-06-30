package com.github.hank9999.kook.handler.types

import kotlin.reflect.KFunction

data class MessageClassHandler(
    val classInstance: Any,
    val function: KFunction<*>
)
