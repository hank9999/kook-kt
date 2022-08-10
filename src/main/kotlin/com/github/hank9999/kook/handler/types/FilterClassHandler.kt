package com.github.hank9999.kook.handler.types

import com.github.hank9999.kook.types.types.ChannelPrivacyTypes
import kotlin.reflect.KFunction

data class FilterClassHandler(
    val type: FilterTypes,
    val classInstance: Any,
    val function: KFunction<*>,
    val channelPrivacyTypes: ChannelPrivacyTypes,
    val filterString: String = "",
    val ignoreCase: Boolean = true,
    val filterRegex: Regex = Regex("")
)
