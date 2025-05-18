package com.github.hank9999.kook.gateway
import com.github.hank9999.kook.common.entity.ChannelPrivacyType
import com.github.hank9999.kook.common.entity.MessageType
import com.github.hank9999.kook.common.utils.JSON.Int
import com.github.hank9999.kook.common.utils.JSON.String
import com.github.hank9999.kook.common.utils.JSON.get
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.DeserializationStrategy as KDeserializationStrategy

sealed class Event {
    abstract val sn: Int?

    object DeserializationStrategy : KDeserializationStrategy<Event> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.github.hank9999.kook.gateway.Event") {
            element<SignalType>("s")
            element<Int>("sn", isOptional = true)
            element<JsonElement>("d", isOptional = true)
        }

        override fun deserialize(decoder: Decoder): Event = decoder.decodeStructure(descriptor) {
            var s: SignalType? = null
            var sn: Int? = null
            var d: JsonElement? = null

            while (true) {
                @OptIn(ExperimentalSerializationApi::class)
                when (val index = decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> s = decodeSerializableElement(descriptor, index, SignalType.serializer())
                    1 -> sn = decodeNullableSerializableElement(descriptor, index, Int.serializer())
                    2 -> d = decodeNullableSerializableElement(descriptor, index, JsonElement.serializer())
                    else -> throw SerializationException("Unknown index $index in Event deserialization")
                }
            }

            when (s) {
                null -> {
                    @OptIn(ExperimentalSerializationApi::class)
                    throw MissingFieldException("s", descriptor.serialName)
                }
                SignalType.SERVER_MESSAGE -> {
                    if (d == null) {
                        @OptIn(ExperimentalSerializationApi::class)
                        throw MissingFieldException("d", descriptor.serialName)
                    }
                    decodeMessageEvent(decoder, d, sn)
                }
                else -> TODO()
            }
        }

        private fun decodeMessageEvent(decoder: Decoder, eventData: JsonElement, sn: Int?): MessageEvent {
            val typeValue = eventData["type"]?.Int ?: throw SerializationException("Missing field 'type' in MessageEvent deserialization")
            val channelTypeValue = eventData["channel_type"]?.String ?: throw SerializationException("Missing field 'channel_type' in MessageEvent deserialization")

            val messageType = MessageType.fromValue(typeValue)
            val channelPrivacyType = ChannelPrivacyType.fromValue(channelTypeValue)

            if (messageType == MessageType.SYSTEM && channelTypeValue == "WEBHOOK_CHALLENGE") {
                val challenge = eventData["challenge"]?.String ?: run {
                    throw SerializationException("Missing field 'challenge' in ChallengeEvent deserialization")
                }
                return ChallengeEvent(challenge, eventData["verify_token"]?.String)
            }

            TODO()
        }
    }
}

sealed class MessageEvent : Event() {
    abstract val verifyToken: String?
}

data class ChallengeEvent(
    val challenge: String,
    override val verifyToken: String? = null,
    override val sn: Int? = null,
) : MessageEvent()