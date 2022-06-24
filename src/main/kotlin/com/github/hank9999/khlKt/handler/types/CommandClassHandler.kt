package com.github.hank9999.khlKt.handler.types

import kotlin.reflect.KFunction

data class CommandClassHandler(
    val classInstance: Any,
    val function: KFunction<*>,
    val name: String,
    val prefixes: List<String>,
    val aliases: List<String>,
    val ignoreCase: Boolean
)
