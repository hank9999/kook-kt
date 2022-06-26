package com.github.hank9999.khlKt.handler.types

import com.github.hank9999.khlKt.types.KhlMessage
import kotlinx.coroutines.CoroutineScope

data class CommandFuncHandler(
    val function: (msg: KhlMessage, cs: CoroutineScope) -> Unit,
    val startWith: List<String>,
    val ignoreCase: Boolean
)
