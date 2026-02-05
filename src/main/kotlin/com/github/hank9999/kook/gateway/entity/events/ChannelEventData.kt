package com.github.hank9999.kook.gateway.entity.events

import com.github.hank9999.kook.common.entity.Attachment
import com.github.hank9999.kook.common.entity.Channel
import com.github.hank9999.kook.common.entity.Emoji
import com.github.hank9999.kook.common.entity.KMarkdown
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * 表情反应事件数据
 * 用于 added_reaction 和 deleted_reaction 事件
 */
@Serializable
data class ReactionEventData(
    @SerialName("msg_id") val msgId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("channel_id") val channelId: String,
    val emoji: Emoji,
)

/**
 * 消息更新事件数据
 * 用于 updated_message 事件
 */
@Serializable
data class MessageUpdatedEventData(
    @SerialName("msg_id") val msgId: String,
    @SerialName("channel_id") val channelId: String,
    val content: String,
    val mention: List<String> = emptyList(),
    @SerialName("mention_all") val mentionAll: Boolean = false,
    @SerialName("mention_here") val mentionHere: Boolean = false,
    @SerialName("mention_roles") val mentionRoles: List<Int> = emptyList(),
    @SerialName("updated_at") val updatedAt: Long,
    val kmarkdown: KMarkdown? = null,
    val quote: JsonElement? = null,
    @SerialName("attachments") val attachment: Attachment? = null,
)

/**
 * 消息删除事件数据
 * 用于 deleted_message 事件
 */
@Serializable
data class MessageDeletedEventData(
    @SerialName("msg_id") val msgId: String,
    @SerialName("channel_id") val channelId: String,
)

/**
 * 消息置顶事件数据
 * 用于 pinned_message 和 unpinned_message 事件
 */
@Serializable
data class MessagePinEventData(
    @SerialName("channel_id") val channelId: String,
    @SerialName("operator_id") val operatorId: String,
    @SerialName("msg_id") val msgId: String,
)

/**
 * 频道删除事件数据
 * 用于 deleted_channel 事件
 */
@Serializable
data class ChannelDeletedEventData(
    val id: String,
    @SerialName("deleted_at") val deletedAt: Long,
)

/**
 * 消息嵌入更新事件数据
 * 用于 embeds_append 事件
 */
@Serializable
data class EmbedsAppendEventData(
    @SerialName("rong_id") val msgId: String,
    @SerialName("channel_id") val channelId: String,
    @SerialName("guild_id") val guildId: String,
    val embeds: List<JsonElement> = emptyList(),
)

/**
 * 频道排序事件数据
 * 用于 sort_channel 事件
 */
@Serializable
data class ChannelSortEventData(
    @SerialName("guild_id") val guildId: String,
    val channels: List<ChannelSortCategory>,
) {
    @Serializable
    data class ChannelSortCategory(
        val id: String,
        val channels: List<String>,
    )
}

/**
 * 批量更新频道事件数据
 * 用于 batch_updated_channel 事件
 */
@Serializable
data class ChannelBatchUpdateEventData(
    @SerialName("added_channel") val addedChannel: Channel? = null,
    @SerialName("updated_channel") val updatedChannels: List<Channel> = emptyList(),
)

/**
 * 批量删除频道事件数据项
 * 用于 batch_deleted_channel 事件
 */
@Serializable
data class ChannelBatchDeleteEventItemData(
    val id: String,
)
