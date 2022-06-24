package com.github.hank9999.khlKt.handler.types

import kotlin.reflect.KFunction

data class CommandClassHandler(
    val classInstance: Any,
    val function: KFunction<*>,
    val startWith: List<String>,
    val ignoreCase: Boolean
)
