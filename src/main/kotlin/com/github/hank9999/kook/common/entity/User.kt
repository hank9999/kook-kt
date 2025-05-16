package com.github.hank9999.kook.common.entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

@Serializable(with = UserSerializer::class)
sealed interface IUser {
    val id: String
    val username: String
    val nickname: String
    val identifyNum: String
    val online: Boolean
    val os: String
    val status: Int
    val avatar: String
    val vipAvatar: String
    val banner: String
    val isVip: Boolean
    val vipAmp: Boolean
    val bot: Boolean
    val roles: List<Int>
    val isSys: Boolean
}

sealed interface IGuildUser : IUser {
    // --- Guild 特有字段 ---
    val mobileVerified: Boolean
    val joinedAt: Long
    val activeTime: Long
    // nameplate 未知用途 List类型 内部类型未知 仅在permission_users的user中

    // decorations_id_map kv类型 未知用途 仅在permission_users的user中, 下为该字段中已知的字段
    // "join_voice": Int,
    // "avatar_border": Int,
    // "background": Int,
    // "nameplate": Int,
    // "nameplates": List<Int>
}

@Serializable
data class User(
    override val id: String,
    override val username: String,
    override val nickname: String,
    @SerialName("identify_num") override val identifyNum: String,
    override val online: Boolean,
    override val os: String,
    override val status: Int,
    override val avatar: String,
    @SerialName("vip_avatar") override val vipAvatar: String,
    override val banner: String,
    @SerialName("is_vip") override val isVip: Boolean,
    @SerialName("vip_amp") override val vipAmp: Boolean,
    override val bot: Boolean,
    override val roles: List<Int>,
    @SerialName("is_sys") override val isSys: Boolean
) : IUser

@Serializable
data class SelfUser(
    override val id: String,
    override val username: String,
    override val nickname: String,
    @SerialName("identify_num") override val identifyNum: String,
    override val online: Boolean,
    override val os: String,
    override val status: Int,
    override val avatar: String,
    @SerialName("vip_avatar") override val vipAvatar: String,
    override val banner: String,
    @SerialName("is_vip") override val isVip: Boolean,
    @SerialName("vip_amp") override val vipAmp: Boolean,
    override val bot: Boolean,
    override val roles: List<Int>,
    @SerialName("is_sys") override val isSys: Boolean,

    // --- user/me 机器人特有字段 ---
    @SerialName("bot_status") val botStatus: Int,
    @SerialName("tag_info") val tagInfo: TagInfo,
    @SerialName("mobile_verified") val mobileVerified: Boolean,
    @SerialName("client_id") val clientId: String,
    val verified: Boolean,
    @SerialName("mobile_prefix") val mobilePrefix: String,
    val mobile: String,
    @SerialName("invited_count") val invitedCount: Int
) : IUser

@Serializable
data class GuildUser(
    override val id: String,
    override val username: String,
    override val nickname: String,
    @SerialName("identify_num") override val identifyNum: String,
    override val online: Boolean,
    override val os: String,
    override val status: Int,
    override val avatar: String,
    @SerialName("vip_avatar") override val vipAvatar: String,
    override val banner: String,
    @SerialName("is_vip") override val isVip: Boolean,
    @SerialName("vip_amp") override val vipAmp: Boolean,
    override val bot: Boolean,
    override val roles: List<Int>,
    @SerialName("is_sys") override val isSys: Boolean,
    @SerialName("mobile_verified") override val mobileVerified: Boolean,
    @SerialName("joined_at") override val joinedAt: Long,
    @SerialName("active_time") override val activeTime: Long
) : IGuildUser

@Serializable
data class GuildBotUser(
    override val id: String,
    override val username: String,
    override val nickname: String,
    @SerialName("identify_num") override val identifyNum: String,
    override val online: Boolean,
    override val os: String,
    override val status: Int,
    override val avatar: String,
    @SerialName("vip_avatar") override val vipAvatar: String,
    override val banner: String,
    @SerialName("is_vip") override val isVip: Boolean,
    @SerialName("vip_amp") override val vipAmp: Boolean,
    override val bot: Boolean,
    override val roles: List<Int>,
    @SerialName("is_sys") override val isSys: Boolean,
    @SerialName("mobile_verified") override val mobileVerified: Boolean,
    @SerialName("joined_at") override val joinedAt: Long,
    @SerialName("active_time") override val activeTime: Long,

    // --- Bot 特有字段 ---
    @SerialName("bot_status") val botStatus: Int,
    @SerialName("tag_info") val tagInfo: TagInfo,
    @SerialName("client_id") val clientId: String,
    val verified: Boolean,
) : IGuildUser

internal object UserSerializer : KSerializer<IUser> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.github.hank9999.kook.common.interfaces.IUser")

    override fun serialize(encoder: Encoder, value: IUser) {
        when (value) {
            is User -> encoder.encodeSerializableValue(User.serializer(), value)
            is SelfUser -> encoder.encodeSerializableValue(SelfUser.serializer(), value)
            is GuildUser -> encoder.encodeSerializableValue(GuildUser.serializer(), value)
            is GuildBotUser -> encoder.encodeSerializableValue(GuildBotUser.serializer(), value)
        }
    }

    override fun deserialize(decoder: Decoder): IUser {
        val jsonDecoder = decoder as JsonDecoder
        val jsonElement = jsonDecoder.decodeJsonElement()
        val jsonObject = jsonElement.jsonObject
        val isBot = jsonObject["bot"]?.jsonPrimitive?.booleanOrNull ?: false
        val isGuild = jsonObject["joined_at"]?.jsonPrimitive?.longOrNull != null
        val isSelf = jsonObject["bot_status"]?.jsonPrimitive?.booleanOrNull ?: false

        return when {
            isSelf -> jsonDecoder.json.decodeFromJsonElement(SelfUser.serializer(), jsonElement)
            isGuild && isBot -> jsonDecoder.json.decodeFromJsonElement(GuildBotUser.serializer(), jsonElement)
            isGuild -> jsonDecoder.json.decodeFromJsonElement(GuildUser.serializer(), jsonElement)
            else -> jsonDecoder.json.decodeFromJsonElement(User.serializer(), jsonElement)
        }
    }
}