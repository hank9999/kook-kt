package com.github.hank9999.kook.types.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Serializable(with = MessageTypes.MessageTypesSerializer::class)
enum class MessageTypes(val type: Int) {
    @Transient NONE(-1),
    @Transient ALL(0),
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
        private val logger: Logger = LoggerFactory.getLogger(MessageTypes::class.java)

        fun fromInt(type: Int): MessageTypes {
            val result = MessageTypes.values().find { it.type == type }
            if (result == null) {
                logger.error("unknown message type: $type")
                return NONE
            }
            return result
        }
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