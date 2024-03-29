package com.github.hank9999.kook.handler.types

import com.github.hank9999.kook.types.Message
import com.github.hank9999.kook.types.types.ChannelPrivacyTypes
import kotlinx.coroutines.CoroutineScope

data class FilterFuncHandler(
    val type: FilterTypes,
    val function: (msg: Message, cs: CoroutineScope) -> Unit,
    val channelPrivacyTypes: ChannelPrivacyTypes,
    val filterString: String = "",
    val ignoreCase: Boolean = true,
    val filterRegex: Regex = Regex("")
)
