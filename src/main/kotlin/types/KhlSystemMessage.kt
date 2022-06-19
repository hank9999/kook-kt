package types

import json.JSON.Companion.json
import types.types.ChannelPrivacyTypes
import types.types.EventTypes
import types.types.MessageTypes
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement


@Serializable
data class KhlSystemMessage(
    @Required val channel_type: ChannelPrivacyTypes = ChannelPrivacyTypes.GROUP,
    @Required val type: MessageTypes = MessageTypes.SYS,
    @Required val target_id: String = "",
    @Required val author_id: String = "",
    @Required val content: String = "",
    @Required val msg_id: String = "",
    @Required val msg_timestamp: Long  = 0,
    @Required val extra: Extra = Extra(),
    @Required val nonce: String = "",
    val verify_token: String = ""
) {
    @Serializable
    data class Extra(
        @Required val type: EventTypes = EventTypes.GUILD_MEMBER_ONLINE,
        val body: JsonElement = json.parseToJsonElement("{}"),
    )
}