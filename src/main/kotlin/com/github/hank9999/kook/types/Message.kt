package com.github.hank9999.kook.types

import com.github.hank9999.kook.types.kmd.KMarkdown
import com.github.hank9999.kook.types.types.ChannelPrivacyTypes
import com.github.hank9999.kook.types.types.MessageTypes
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class Message(
    @Required val channel_type: ChannelPrivacyTypes = ChannelPrivacyTypes.GROUP,
    @Required val type: MessageTypes = MessageTypes.KMD,
    @Required val target_id: String = "",
    @Required val author_id: String = "",
    @Required val content: String = "",
    @Required val extra: Extra = Extra(),
    @Required val msg_id: String = "",
    @Required val msg_timestamp: Long = 0,
    @Required val nonce: String = "",
    val verify_token: String = ""
) {
    @Serializable
    data class Extra(
        @Required val type: MessageTypes = MessageTypes.KMD,
        val guild_id: String = "",
        val channel_name: String = "",
        val mention: List<String> = emptyList(),
        val mention_all: Boolean = false,
        val mention_roles: List<Int> = emptyList(),
        val mention_here: Boolean = false,
        val nav_channels: List<JsonElement> = emptyList(),
        val code: String = "",
        val author: User = User(),
        val kmarkdown: KMarkdown = KMarkdown(),
        val quote: Quote = Quote(),
        val attachments: Attachments = Attachments()
    )
}