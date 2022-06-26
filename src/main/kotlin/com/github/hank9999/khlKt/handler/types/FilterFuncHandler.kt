package com.github.hank9999.khlKt.handler.types

import com.github.hank9999.khlKt.types.KhlMessage
import kotlinx.coroutines.CoroutineScope

data class FilterFuncHandler(
    val type: FilterTypes,
    val function: (msg: KhlMessage, cs: CoroutineScope) -> Unit,
    val filterString: String = "",
    val ignoreCase: Boolean = true,
    val filterRegex: Regex = Regex("")
)
