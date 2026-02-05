package com.github.hank9999.kook.gateway.entity.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 用户信息更新事件数据
 * 用于 user_updated 事件
 */
@Serializable
data class UserUpdatedEventData(
    @SerialName("user_id") val userId: String,
    val username: String,
    val avatar: String,
)

/**
 * 自己加入服务器事件数据
 * 用于 self_joined_guild 事件
 */
@Serializable
data class SelfJoinedGuildEventData(
    @SerialName("guild_id") val guildId: String,
    val state: String? = null,
)

/**
 * 自己退出服务器事件数据
 * 用于 self_exited_guild 事件
 */
@Serializable
data class SelfExitedGuildEventData(
    @SerialName("guild_id") val guildId: String,
)
