package com.github.hank9999.kook.gateway.entity.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 成员加入服务器事件数据
 * 用于 joined_guild 事件
 */
@Serializable
data class MemberJoinedEventData(
    @SerialName("user_id") val userId: String,
    @SerialName("joined_at") val joinedAt: Long,
)

/**
 * 成员退出服务器事件数据
 * 用于 exited_guild 事件
 */
@Serializable
data class MemberExitedEventData(
    @SerialName("user_id") val userId: String,
    @SerialName("exited_at") val exitedAt: Long,
)

/**
 * 成员信息更新事件数据
 * 用于 updated_guild_member 事件
 */
@Serializable
data class MemberUpdatedEventData(
    @SerialName("user_id") val userId: String,
    @SerialName("is_vip") val isVip: Boolean? = null,
    val nickname: String = "",
)

/**
 * 成员上线/下线事件数据
 * 用于 guild_member_online 和 guild_member_offline 事件
 */
@Serializable
data class MemberOnlineStatusEventData(
    @SerialName("user_id") val userId: String,
    @SerialName("event_time") val eventTime: Long,
    val guilds: List<String>,
)
