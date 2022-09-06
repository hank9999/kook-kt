package com.github.hank9999.kook.types

import com.github.hank9999.kook.json.JSON.Companion.json
import com.github.hank9999.kook.types.types.ChannelPrivacyTypes
import com.github.hank9999.kook.types.types.EventTypes
import com.github.hank9999.kook.types.types.MessageTypes
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


@Serializable
data class Event(
    @Required @SerialName("channel_type") val channelType: ChannelPrivacyTypes = ChannelPrivacyTypes.GROUP,
    @Required val type: MessageTypes = MessageTypes.SYS,
    @Required @SerialName("target_id") val targetId: String = "",
    @Required @SerialName("author_id") val authorId: String = "",
    @Required val content: String = "",
    @Required @SerialName("msg_id") val msgId: String = "",
    @Required @SerialName("msg_timestamp") val msgTimestamp: Long  = 0,
    @Required val extra: Extra = Extra(),
    @Required val nonce: String = "",
    @SerialName("verify_token") val verifyToken: String = "",
    @SerialName("from_type") val fromType: Int = 1
) {
    @Serializable
    data class Extra(
        @Required val type: EventTypes = EventTypes.GUILD_MEMBER_ONLINE,
        val body: JsonElement = json.parseToJsonElement("{}"),
    )
}