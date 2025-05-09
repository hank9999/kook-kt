package com.github.hank9999.kook.common.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Guild(
    val id: String,
    val name: String,
    val topic: String,
    @SerialName("user_id") val userId: String,
    val icon: String,
    @SerialName("notify_type") val notifyType: Int,
    val region: String,
    @SerialName("enable_open") val enableOpen: Boolean,
    @SerialName("open_id") val openId: String,
    @SerialName("default_channel_id") val defaultChannelId: String,
    @SerialName("welcome_channel_id") val welcomeChannelId: String,
    val roles: List<Role>,
    val channels: List<IChannel>
)