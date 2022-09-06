package com.github.hank9999.kook.types.kmd

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChannelPart(
    @SerialName("guild_id") val guildId: String = "",
    val id: String = "",
    val name: String = ""
)