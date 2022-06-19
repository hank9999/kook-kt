package com.github.hank9999.khlKt.types.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = TextTypes.TextTypesSerializer::class)
enum class TextTypes(val type: String) {
    PLAIN("plain-text"),
    KMD("kmarkdown");

    companion object {
        fun fromString(type: String) = TextTypes.values().first { it.type == type }
    }

    object TextTypesSerializer : KSerializer<TextTypes> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("type", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: TextTypes) {
            encoder.encodeString(value.type)
        }

        override fun deserialize(decoder: Decoder): TextTypes {
            return fromString(decoder.decodeString())
        }
    }
}