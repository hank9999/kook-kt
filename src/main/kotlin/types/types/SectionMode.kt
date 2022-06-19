package types.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

enum class SectionMode(val mode: String) {
    RIGHT("right"),
    LEFT("left");

    companion object {
        fun fromString(mode: String) = SectionMode.values().first { it.mode == mode }
    }

    object SectionModeSerializer : KSerializer<SectionMode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("mode", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: SectionMode) {
            encoder.encodeString(value.mode)
        }

        override fun deserialize(decoder: Decoder): SectionMode {
            return SectionMode.fromString(decoder.decodeString())
        }
    }
}