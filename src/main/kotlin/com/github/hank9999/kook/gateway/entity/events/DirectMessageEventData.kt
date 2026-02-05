package com.github.hank9999.kook.gateway.entity.events

import com.github.hank9999.kook.common.entity.Attachment
import com.github.hank9999.kook.common.entity.Emoji
import com.github.hank9999.kook.common.entity.KMarkdown
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * 私聊消息更新事件数据
 * 用于 updated_private_message 事件
 */
@Serializable
data class DirectMessageUpdatedEventData(
    @SerialName("msg_id") val msgId: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("target_id") val targetId: String,
    val content: String,
    @SerialName("chat_code") val chatCode: String,
    @SerialName("updated_at") val updatedAt: Long,
    val kmarkdown: KMarkdown? = null,
    val quote: JsonElement? = null,
    @SerialName("attachments") val attachment: Attachment? = null,
)

/**
 * 私聊消息删除事件数据
 * 用于 deleted_private_message 事件
 */
@Serializable
data class DirectMessageDeletedEventData(
    @SerialName("msg_id") val msgId: String,
    @SerialName("author_id") val authorId: String,
    @SerialName("target_id") val targetId: String,
    @SerialName("chat_code") val chatCode: String,
    @SerialName("deleted_at") val deletedAt: Long,
)

/**
 * 私聊表情反应事件数据
 * 用于 private_added_reaction 和 private_deleted_reaction 事件
 */
@Serializable
data class DirectReactionEventData(
    @SerialName("msg_id") val msgId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("chat_code") val chatCode: String,
    val emoji: Emoji,
)
