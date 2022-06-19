package com.github.hank9999.khlKt.types.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Theme.ThemeSerializer::class)
enum class Theme(val theme: String) {
    NA(""),
    PRIMARY("primary"),
    SECONDARY("secondary"),
    SUCCESS("success"),
    DANGER("danger"),
    WARNING("warning"),
    INFO("info"),
    NONE("none");

    companion object {
        fun fromString(theme: String) = Theme.values().first { it.theme == theme }
    }

    object ThemeSerializer : KSerializer<Theme> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("theme", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Theme) {
            encoder.encodeString(value.theme)
        }

        override fun deserialize(decoder: Decoder): Theme {
            return fromString(decoder.decodeString())
        }
    }
}