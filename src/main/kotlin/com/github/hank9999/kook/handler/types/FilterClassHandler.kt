package com.github.hank9999.kook.handler.types

import kotlin.reflect.KFunction

data class FilterClassHandler(
    val type: FilterTypes,
    val classInstance: Any,
    val function: KFunction<*>,
    val filterString: String = "",
    val ignoreCase: Boolean = true,
    val filterRegex: Regex = Regex("")
)
