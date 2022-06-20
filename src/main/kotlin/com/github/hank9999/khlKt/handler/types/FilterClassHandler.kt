package com.github.hank9999.khlKt.handler.types

import kotlin.reflect.KFunction

data class FilterClassHandler(
    val type: FilterTypes,
    val filterString: String = "",
    val filterRegex: Regex = Regex(""),
    val ignoreCase: Boolean = true,
    val classInstance: Any,
    val function: KFunction<*>
)
