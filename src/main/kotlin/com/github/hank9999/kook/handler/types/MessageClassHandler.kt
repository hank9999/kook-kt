package com.github.hank9999.kook.handler.types

import com.github.hank9999.kook.types.types.ChannelPrivacyTypes
import kotlin.reflect.KFunction

data class MessageClassHandler(
    val classInstance: Any,
    val channelPrivacyTypes: ChannelPrivacyTypes,
    val function: KFunction<*>
)
