package com.github.hank9999.khlKt.types.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

enum class Click(val click: String) {
    NA(""),
    LINK("link"),
    RETURN_VAL("return-val");

    companion object {
        fun fromString(click: String) = Click.values().first { it.click == click }
    }

    object ClickSerializer : KSerializer<Click> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("click", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Click) {
            encoder.encodeString(value.click)
        }

        override fun deserialize(decoder: Decoder): Click {
            return fromString(decoder.decodeString())
        }
    }
}