package types.types

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = FileTypes.FileTypesSerializer::class)
enum class FileTypes(val type: String) {
    FILE("file"),
    AUDIO("audio"),
    VIDEO("video");

    companion object {
        fun fromString(type: String) = FileTypes.values().first { it.type == type }
    }

    object FileTypesSerializer : KSerializer<FileTypes> {
        override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("type", PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: FileTypes) {
            encoder.encodeString(value.type)
        }

        override fun deserialize(decoder: Decoder): FileTypes {
            return FileTypes.fromString(decoder.decodeString())
        }
    }
}