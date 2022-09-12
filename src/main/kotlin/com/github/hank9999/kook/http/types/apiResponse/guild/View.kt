package com.github.hank9999.kook.http.types.apiResponse.guild

import com.github.hank9999.kook.types.Channel
import com.github.hank9999.kook.types.Role
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class View(
    @Required val id: String = "",
    @Required val name: String = "",
    @Required val topic: String = "",
    @Required @SerialName("master_id") val masterId: String = "",
    @Required val icon: String = "",
    @Required @SerialName("notify_type") val notifyType: Int = 0,
    @Required val region: String = "",
    @Required @SerialName("enable_open") val enableOpen: Boolean = false,
    @Required @SerialName("open_id") val openId: String = "",
    @Required @SerialName("default_channel_id") val defaultChannelId: String = "",
    @Required @SerialName("welcome_channel_id") val welcomeChannelId: String = "",
    @Required val roles: List<Role> = emptyList(),
    @Required val channels: List<Channel> = emptyList(),
)