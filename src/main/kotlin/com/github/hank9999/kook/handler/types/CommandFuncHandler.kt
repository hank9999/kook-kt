package com.github.hank9999.kook.handler.types

import com.github.hank9999.kook.types.Message
import com.github.hank9999.kook.types.types.ChannelPrivacyTypes
import kotlinx.coroutines.CoroutineScope

data class CommandFuncHandler(
    val function: (msg: Message, cs: CoroutineScope) -> Unit,
    val startWith: List<String>,
    val ignoreCase: Boolean,
    val channelPrivacyTypes: ChannelPrivacyTypes,
)
