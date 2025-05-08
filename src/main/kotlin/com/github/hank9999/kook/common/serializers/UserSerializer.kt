package com.github.hank9999.kook.common.serializers

import com.github.hank9999.kook.common.entity.user.GuildBotUser
import com.github.hank9999.kook.common.entity.user.GuildUser
import com.github.hank9999.kook.common.entity.user.SelfUser
import com.github.hank9999.kook.common.entity.user.User
import com.github.hank9999.kook.common.interfaces.IUser
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

object UserSerializer : KSerializer<IUser> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.github.hank9999.kook.common.interfaces.IUser")

    override fun serialize(encoder: Encoder, value: IUser) {
        when (value) {
            is User -> encoder.encodeSerializableValue(User.serializer(), value)
            is SelfUser -> encoder.encodeSerializableValue(SelfUser.serializer(), value)
            is GuildUser -> encoder.encodeSerializableValue(GuildUser.serializer(), value)
            is GuildBotUser -> encoder.encodeSerializableValue(GuildBotUser.serializer(), value)
            else -> throw SerializationException("${value::class.simpleName} is not a valid IUser type for UserSerializer")
        }
    }

    override fun deserialize(decoder: Decoder): IUser {
        val jsonDecoder = decoder as JsonDecoder
        val jsonElement = jsonDecoder.decodeJsonElement()
        val jsonObject = jsonElement.jsonObject
        val isBot = jsonObject["bot"]?.jsonPrimitive?.booleanOrNull ?: false
        val isGuild = jsonObject["joined_at"]?.jsonPrimitive?.longOrNull != null
        val isSelf = jsonObject["bot_status"]?.jsonPrimitive?.booleanOrNull ?: false

        if (isSelf) {
            return jsonDecoder.json.decodeFromJsonElement(SelfUser.serializer(), jsonElement)
        }
        if (isGuild && isBot) {
            return jsonDecoder.json.decodeFromJsonElement(GuildBotUser.serializer(), jsonElement)
        }
        if (isGuild) {
            return jsonDecoder.json.decodeFromJsonElement(GuildUser.serializer(), jsonElement)
        }
        return jsonDecoder.json.decodeFromJsonElement(User.serializer(), jsonElement)
    }
}