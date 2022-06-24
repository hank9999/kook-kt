package com.github.hank9999.khlKt.handler.types

import com.github.hank9999.khlKt.types.KhlMessage

data class CommandFuncHandler(
    val function: (msg: KhlMessage) -> Unit,
    val name: String,
    val prefixes: List<String>,
    val aliases: List<String>,
    val ignoreCase: Boolean
)
