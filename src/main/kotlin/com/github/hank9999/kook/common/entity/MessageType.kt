package com.github.hank9999.kook.common.entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MessageType.MessageTypesSerializer::class)
enum class MessageType(val type: Int) {
    @Transient UNKNOWN(-1),
    TEXT(1),
    IMG(2),
    VIDEO(3),
    FILE(4),
    AUDIO(8),
    KMD(9),
    CARD(10),
    ITEM(12),
    SYS(255);

    companion object {
        fun fromValue(value: Int): MessageType = entries.firstOrNull { it.type == value } ?: UNKNOWN
    }

    object MessageTypesSerializer : KSerializer<MessageType> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("com.github.hank9999.kook.common.entity.MessageType", PrimitiveKind.INT)
        override fun serialize(encoder: Encoder, value: MessageType) = encoder.encodeInt(value.type)
        override fun deserialize(decoder: Decoder): MessageType = MessageType.fromValue(decoder.decodeInt())
    }
}