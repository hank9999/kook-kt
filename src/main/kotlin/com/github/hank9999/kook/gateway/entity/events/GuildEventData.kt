package com.github.hank9999.kook.gateway.entity.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 服务器事件数据
 * 用于 updated_guild 和 deleted_guild 事件
 */
@Serializable
data class GuildEventData(
    val id: String,
    val name: String,
    @SerialName("user_id") val userId: String,
    val icon: String = "",
    @SerialName("notify_type") val notifyType: Int = 1,
    val region: String = "",
    @SerialName("enable_open") val enableOpen: Int = 0,
    @SerialName("open_id") val openId: Int = 0,
    @SerialName("default_channel_id") val defaultChannelId: String = "",
    @SerialName("welcome_channel_id") val welcomeChannelId: String = "",
)

/**
 * 封禁列表添加事件数据
 * 用于 added_block_list 事件
 */
@Serializable
data class BlockListAddedEventData(
    @SerialName("operator_id") val operatorId: String,
    val remark: String = "",
    @SerialName("user_id") val userIds: List<String>,
)

/**
 * 封禁列表删除事件数据
 * 用于 deleted_block_list 事件
 */
@Serializable
data class BlockListDeletedEventData(
    @SerialName("operator_id") val operatorId: String,
    @SerialName("user_id") val userIds: List<String>,
)

/**
 * 表情事件数据
 * 用于 added_emoji、removed_emoji、updated_emoji 事件
 */
@Serializable
data class EmojiEventData(
    val id: String,
    val name: String,
)
