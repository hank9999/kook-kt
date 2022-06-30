package com.github.hank9999.kook.types.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = EventTypes.EventTypesSerializer::class)
enum class EventTypes(val type: String) {
    @Transient ALL(""),
    MESSAGE_BTN_CLICK("message_btn_click"),
    ADDED_REACTION("added_reaction"),
    DELETED_REACTION("deleted_reaction"),
    UPDATED_MESSAGE("updated_message"),
    DELETED_MESSAGE("deleted_message"),
    PRIVATE_ADDED_REACTION("private_added_reaction"),
    PRIVATE_DELETED_REACTION("private_deleted_reaction"),
    UPDATED_PRIVATE_MESSAGE("updated_private_message"),
    DELETED_PRIVATE_MESSAGE("deleted_private_message"),
    UPDATED_GUILD("updated_guild"),
    DELETED_GUILD("deleted_guild"),
    ADDED_BLOCK_LIST("added_block_list"),
    DELETED_BLOCK_LIST("deleted_block_list"),
    ADDED_ROLE("added_role"),
    DELETED_ROLE("deleted_role"),
    UPDATED_ROLE("updated_role"),
    JOINED_GUILD("joined_guild"),
    EXITED_GUILD("exited_guild"),
    GUILD_MEMBER_ONLINE("guild_member_online"),
    GUILD_MEMBER_OFFLINE("guild_member_offline"),
    UPDATED_GUILD_MEMBER("updated_guild_member"),
    UPDATED_CHANNEL("updated_channel"),
    ADDED_CHANNEL("added_channel"),
    DELETED_CHANNEL("deleted_channel"),
    JOINED_CHANNEL("joined_channel"),
    EXITED_CHANNEL("exited_channel"),
    USER_UPDATED("user_updated"),
    SELF_JOINED_GUILD("self_joined_guild"),
    SELF_EXITED_GUILD("self_exited_guild"),
    PINNED_MESSAGE("pinned_message"),
    UNPINNED_MESSAGE("unpinned_message"),
    BROADCAST("broadcast");

    companion object {
        fun fromString(type: String) = EventTypes.values().first { it.type == type }
    }

    object EventTypesSerializer : KSerializer<EventTypes> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("type", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: EventTypes) {
            encoder.encodeString(value.type)
        }

        override fun deserialize(decoder: Decoder): EventTypes {
            return fromString(decoder.decodeString())
        }
    }
}