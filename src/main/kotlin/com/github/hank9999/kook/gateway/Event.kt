package com.github.hank9999.kook.gateway
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.DeserializationStrategy as KDeserializationStrategy

sealed class Event {
    object DeserializationStrategy : KDeserializationStrategy<Event> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.github.hank9999.kook.gateway.Event") {
            element("s", SignalType.serializer().descriptor)
            element("sn", Int.serializer().descriptor.nullable)
            element("d", JsonElement.serializer().descriptor.nullable)
        }

        override fun deserialize(decoder: Decoder): Event {
            TODO("Not yet implemented")
        }

    }
}