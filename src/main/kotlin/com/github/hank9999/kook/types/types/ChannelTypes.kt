package com.github.hank9999.kook.types.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ChannelTypes.ChannelTypesSerializer::class)
enum class ChannelTypes(val type: Int) {
    CATEGORY(0),
    TEXT(1),
    AUDIO(2);

    companion object {
        fun fromInt(type: Int) = ChannelTypes.values().first { it.type == type }
    }

    object ChannelTypesSerializer : KSerializer<ChannelTypes> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("type", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: ChannelTypes) {
            encoder.encodeInt(value.type)
        }

        override fun deserialize(decoder: Decoder): ChannelTypes {
            return fromInt(decoder.decodeInt())
        }
    }
}