package com.github.hank9999.kook.handler.types

import com.github.hank9999.kook.types.Message
import kotlinx.coroutines.CoroutineScope

data class FilterFuncHandler(
    val type: FilterTypes,
    val function: (msg: Message, cs: CoroutineScope) -> Unit,
    val filterString: String = "",
    val ignoreCase: Boolean = true,
    val filterRegex: Regex = Regex("")
)
