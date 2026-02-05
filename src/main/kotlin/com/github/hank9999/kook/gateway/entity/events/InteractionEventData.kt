package com.github.hank9999.kook.gateway.entity.events

import com.github.hank9999.kook.common.entity.IUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 按钮点击事件数据
 * 用于 message_btn_click 事件
 */
@Serializable
data class ButtonClickedEventData(
    val value: String,
    @SerialName("msg_id") val msgId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("target_id") val targetId: String,
    @SerialName("channel_type") val channelType: String? = null,
    @SerialName("user_info") val userInfo: IUser? = null,
    @SerialName("guild_id") val guildId: String? = null,
)
