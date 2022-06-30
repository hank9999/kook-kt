package com.github.hank9999.kook.types.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = MessageTypes.MessageTypesSerializer::class)
enum class MessageTypes(val type: Int) {
    @Transient ALL(0),
    TEXT(1),
    IMG(2),
    VIDEO(3),
    FILE(4),
    AUDIO(8),
    KMD(9),
    CARD(10),
    SYS(255);

    companion object {
        fun fromInt(type: Int) = MessageTypes.values().first { it.type == type }
    }

    object MessageTypesSerializer : KSerializer<MessageTypes> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("type", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: MessageTypes) {
            encoder.encodeInt(value.type)
        }

        override fun deserialize(decoder: Decoder): MessageTypes {
            return fromInt(decoder.decodeInt())
        }
    }
}