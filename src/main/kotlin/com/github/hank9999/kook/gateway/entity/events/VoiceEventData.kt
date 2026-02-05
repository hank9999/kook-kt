package com.github.hank9999.kook.gateway.entity.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 用户加入语音频道事件数据
 * 用于 joined_channel 事件
 */
@Serializable
data class UserJoinedVoiceEventData(
    @SerialName("user_id") val userId: String,
    @SerialName("channel_id") val channelId: String,
    @SerialName("joined_at") val joinedAt: Long,
)

/**
 * 用户退出语音频道事件数据
 * 用于 exited_channel 事件
 */
@Serializable
data class UserExitedVoiceEventData(
    @SerialName("user_id") val userId: String,
    @SerialName("channel_id") val channelId: String,
    @SerialName("exited_at") val exitedAt: Long,
)
