package com.github.hank9999.kook.gateway.entity.events

import com.github.hank9999.kook.common.entity.Channel
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive

/**
 * 用户加入语音频道事件数据
 * 用于 joined_channel 事件
 */
@Serializable
data class UserJoinedVoiceEventData(
    @SerialName("user_id") val userId: String,
    @SerialName("channel_id") val channelId: String,
    @SerialName("joined_at") val joinedAt: Long,
)

/**
 * 用户退出语音频道事件数据
 * 用于 exited_channel 事件
 */
@Serializable
data class UserExitedVoiceEventData(
    @SerialName("user_id") val userId: String,
    @SerialName("channel_id") val channelId: String,
    @SerialName("exited_at") val exitedAt: Long,
)

@Serializable(with = MuteOrDeafType.MuteOrDeafTypeSerializer::class)
enum class MuteOrDeafType(val code: Int) {
    MUTE(1),
    DEAF(2),
    @Transient UNKNOWN(-1);

    companion object {
        fun fromCode(value: Int): MuteOrDeafType = entries.firstOrNull { it.code == value } ?: UNKNOWN

        fun fromString(value: String): MuteOrDeafType = when (value.lowercase()) {
            "mute" -> MUTE
            "deaf" -> DEAF
            else -> value.toIntOrNull()?.let(::fromCode) ?: UNKNOWN
        }
    }

    internal object MuteOrDeafTypeSerializer : KSerializer<MuteOrDeafType> {
        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor("com.github.hank9999.kook.gateway.entity.events.MuteOrDeafType", PrimitiveKind.INT)

        override fun serialize(encoder: Encoder, value: MuteOrDeafType) = encoder.encodeInt(value.code)

        override fun deserialize(decoder: Decoder): MuteOrDeafType {
            val jsonDecoder = decoder as? JsonDecoder
            if (jsonDecoder != null) {
                val element = jsonDecoder.decodeJsonElement()
                val primitive = element.jsonPrimitive
                primitive.intOrNull?.let { return fromCode(it) }
                return fromString(primitive.content)
            }

            return try {
                fromCode(decoder.decodeInt())
            } catch (_: Exception) {
                fromString(decoder.decodeString())
            }
        }
    }
}

/**
 * 服务器语音闭麦/静音事件数据
 * 用于 add_guild_mute / delete_guild_mute 事件
 */
@Serializable
data class GuildMuteDeafEventData(
    @SerialName("user_id") val userId: String,
    @SerialName("guild_id") val guildId: String,
    val type: MuteOrDeafType = MuteOrDeafType.UNKNOWN,
)

/**
 * 直播状态变更事件数据
 * 用于 live_status_changed 事件
 */
@Serializable
data class LiveStatusChangeEventData(
    val channel: Channel,
    val user: LiveStreamUserEventData,
)

@Serializable
data class LiveStreamUserEventData(
    val id: String,
    val username: String = "",
    @SerialName("identify_num") val identifyNum: String = "",
    val online: Boolean = false,
    val os: String = "",
    val bot: Boolean? = null,
    val status: Int? = null,
    val avatar: String = "",
    @SerialName("vip_avatar") val vipAvatar: String? = null,
    @SerialName("is_vip") val isVip: Boolean? = null,
    @SerialName("vip_amp") val vipAmp: Boolean? = null,
    @SerialName("live_info") val liveInfo: LiveInfoEventData = LiveInfoEventData(),
)

@Serializable
data class LiveInfoEventData(
    @SerialName("in_live") val inLive: Boolean = false,
    @SerialName("audience_count") val audienceCount: Int = 0,
    @SerialName("audience_limit") val audienceLimit: Int = 0,
    @SerialName("live_thumb") val liveThumb: String? = null,
    @SerialName("live_start_time") val liveStartTime: Long? = null,
    val resolution: Int = 0,
    @SerialName("frame_rate") val frameRate: Int = 0,
    val tag: String = "",
    val color: String = "",
    @SerialName("img_url") val imgUrl: String = "",
    val mode: Int = 0,
)
