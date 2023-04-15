package com.github.hank9999.kook.types

import com.github.hank9999.kook.Bot.Companion.kookApi
import com.github.hank9999.kook.http.types.apiResponse.MessageCreate
import com.github.hank9999.kook.types.kmd.KMarkdown
import com.github.hank9999.kook.types.types.ChannelPrivacyTypes
import com.github.hank9999.kook.types.types.MessageTypes
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Message(
    @Required @SerialName("channel_type") val channelType: ChannelPrivacyTypes = ChannelPrivacyTypes.GROUP,
    @Required val type: MessageTypes = MessageTypes.KMD,
    @Required @SerialName("target_id") val targetId: String = "",
    @Required @SerialName("author_id") val authorId: String = "",
    @Required val content: String = "",
    @Required val extra: Extra = Extra(),
    @Required @SerialName("msg_id") val msgId: String = "",
    @Required @SerialName("msg_timestamp") val msgTimestamp: Long = 0,
    @Required val nonce: String = "",
    @SerialName("verify_token") val verifyToken: String = "",
    @SerialName("from_type") val fromType: Int = 0
) {
    @Serializable
    data class Extra(
        @Required val type: MessageTypes = MessageTypes.KMD,
        @SerialName("guild_id") val guildId: String = "",
        @SerialName("channel_name") val channelName: String = "",
        val mention: List<String> = emptyList(),
        @SerialName("mention_all") val mentionAll: Boolean = false,
        @SerialName("mention_roles") val mentionRoles: List<Int> = emptyList(),
        @SerialName("mention_here") val mentionHere: Boolean = false,
        @SerialName("nav_channels") val navChannels: List<String> = emptyList(),
        val code: String = "",
        val author: User = User(),
        val kmarkdown: KMarkdown = KMarkdown(),
        val quote: Quote = Quote(),
        val attachments: Attachments = Attachments(),
        @SerialName("last_msg_content") val lastMsgContent: String = ""
    )

    suspend fun reply(content: Any, type: MessageTypes? = null, nonce: String? = null, tempTargetId: String? = null): MessageCreate {
        if (this.channelType == ChannelPrivacyTypes.PERSON)
            return kookApi.DirectMessage().create(this.authorId, content, type, this.msgId, nonce)
        return kookApi.Message().create(this.targetId, content, type, this.msgId, nonce, tempTargetId)
    }

    suspend fun send(content: Any, type: MessageTypes? = null, quote: String? = null, nonce: String? = null, tempTargetId: String? = null): MessageCreate {
        if (this.channelType == ChannelPrivacyTypes.PERSON)
            return kookApi.DirectMessage().create(this.authorId, content, type, quote, nonce)
        return kookApi.Message().create(this.targetId, content, type, quote, nonce, tempTargetId)
    }
}
