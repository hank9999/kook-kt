package com.github.hank9999.khlKt.handler.types

import com.github.hank9999.khlKt.types.KhlMessage

data class CommandFuncHandler(
    val function: (msg: KhlMessage) -> Unit,
    val startWith: List<String>,
    val ignoreCase: Boolean
)
