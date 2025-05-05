package com.github.hank9999.kook.common.entity.channel

import com.github.hank9999.kook.common.interfaces.IChannel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DetailChannel(
    val channel: Channel,

    // more information from channel/view
    @SerialName("voice_quality") val voiceQuality: String,
    @SerialName("server_url") val serverUrl: String,
    // val children: List<Channel>,
    val region: String,
    @SerialName("sync_guild_region") val syncGuildRegion: Int,
) : IChannel by channel