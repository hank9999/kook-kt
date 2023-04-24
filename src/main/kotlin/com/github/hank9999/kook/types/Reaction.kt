package com.github.hank9999.kook.types

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class Reaction(
    @SerialName("count") val count: Int = 0,
    @SerialName("emoji") val emoji: Emoji = Emoji(),
    @SerialName("me") val me: Boolean = false
)