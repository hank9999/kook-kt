package types.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = CountdownMode.CountdownModeSerializer::class)
enum class CountdownMode(val mode: String) {
    DAY("day"),
    HOUR("hour"),
    SECOND("second");

    companion object {
        fun fromString(mode: String) = CountdownMode.values().first { it.mode == mode }
    }

    object CountdownModeSerializer : KSerializer<CountdownMode> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("mode", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: CountdownMode) {
            encoder.encodeString(value.mode)
        }

        override fun deserialize(decoder: Decoder): CountdownMode {
            return fromString(decoder.decodeString())
        }
    }
}