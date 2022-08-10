package com.github.hank9999.kook.handler.types

import com.github.hank9999.kook.types.types.ChannelPrivacyTypes
import kotlin.reflect.KFunction

data class CommandClassHandler(
    val classInstance: Any,
    val function: KFunction<*>,
    val startWith: List<String>,
    val ignoreCase: Boolean,
    val channelPrivacyTypes: ChannelPrivacyTypes,
)
