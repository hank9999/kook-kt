package com.github.hank9999.kook.types

import com.github.hank9999.kook.json.JSON.Companion.json
import com.github.hank9999.kook.types.types.ChannelPrivacyTypes
import com.github.hank9999.kook.types.types.EventTypes
import com.github.hank9999.kook.types.types.MessageTypes
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


@Serializable
data class Event(
    @Required val channel_type: ChannelPrivacyTypes = ChannelPrivacyTypes.GROUP,
    @Required val type: MessageTypes = MessageTypes.SYS,
    @Required val target_id: String = "",
    @Required val author_id: String = "",
    @Required val content: String = "",
    @Required val msg_id: String = "",
    @Required val msg_timestamp: Long  = 0,
    @Required val extra: Extra = Extra(),
    @Required val nonce: String = "",
    val verify_token: String = "",
    val from_type: Int = 1
) {
    @Serializable
    data class Extra(
        @Required val type: EventTypes = EventTypes.GUILD_MEMBER_ONLINE,
        val body: JsonElement = json.parseToJsonElement("{}"),
    )
}