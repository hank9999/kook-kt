package types.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Size.SizeSerializer::class)
enum class Size(val size: String) {
    NA(""),
    XS("xs"),
    SM("sm"),
    MD("md"),
    LG("lg");

    companion object {
        fun fromString(size: String) = Size.values().first { it.size == size }
    }

    object SizeSerializer : KSerializer<Size> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("size", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: Size) {
            encoder.encodeString(value.size)
        }

        override fun deserialize(decoder: Decoder): Size {
            return Size.fromString(decoder.decodeString())
        }
    }
}