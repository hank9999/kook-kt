package com.github.hank9999.kook.gateway.entity

import com.github.hank9999.kook.common.entity.Attachment
import com.github.hank9999.kook.common.entity.Channel
import com.github.hank9999.kook.common.entity.ChannelPrivacyType
import com.github.hank9999.kook.common.entity.IChannel
import com.github.hank9999.kook.common.entity.IUser
import com.github.hank9999.kook.common.entity.KMarkdown
import com.github.hank9999.kook.common.entity.MessageType
import com.github.hank9999.kook.common.utils.JSON
import com.github.hank9999.kook.common.utils.JSON.Int
import com.github.hank9999.kook.common.utils.JSON.Long
import com.github.hank9999.kook.common.utils.JSON.String
import com.github.hank9999.kook.common.utils.JSON.get
import com.github.hank9999.kook.gateway.entity.events.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

@Serializable(with = Event.EventSerializer::class)
sealed class Event {
    abstract val sn: Int?

    object EventSerializer : KSerializer<Event> {
        override val descriptor: SerialDescriptor = buildClassSerialDescriptor("com.github.hank9999.kook.gateway.entity.Event") {
            element<SignalType>("s")
            element<Int>("sn", isOptional = true)
            element<JsonElement>("d", isOptional = true)
        }

        override fun serialize(encoder: Encoder, value: Event) {
            throw SerializationException("This object cannot be serialized.")
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
                    decodeMessageEvent(d, sn)
                }
                SignalType.SERVER_HANDSHAKE_ACK -> HandshakeAckEvent(sn = sn, data = d)
                SignalType.SERVER_PONG -> PongEvent(sn = sn)
                SignalType.SERVER_REQUEST_RECONNECT -> ReconnectEvent(sn = sn, data = d)
                SignalType.SERVER_RESUME_ACK -> ResumeAckEvent(sn = sn, data = d)
                else -> UnknownEvent(signalType = s, sn = sn, data = d)
            }
        }

        private fun decodeMessageEvent(eventData: JsonElement, sn: Int?): MessageEvent {
            val json = JSON.defaultJson()
            val typeValue = eventData["type"]?.Int
                ?: throw SerializationException("Missing field 'type' in MessageEvent deserialization")
            val channelTypeValue = eventData["channel_type"]?.String
                ?: throw SerializationException("Missing field 'channel_type' in MessageEvent deserialization")

            val messageType = MessageType.fromValue(typeValue)
            val channelPrivacyType = ChannelPrivacyType.fromValue(channelTypeValue)

            // Challenge 事件
            if (messageType == MessageType.SYSTEM && channelTypeValue == "WEBHOOK_CHALLENGE") {
                val challenge = eventData["challenge"]?.String ?: run {
                    throw SerializationException("Missing field 'challenge' in ChallengeEvent deserialization")
                }
                return ChallengeEvent(challenge, eventData["verify_token"]?.String)
            }

            val targetId = eventData["target_id"]?.String ?: ""
            val authorId = eventData["author_id"]?.String ?: ""
            val content = eventData["content"]?.String ?: ""
            val msgId = eventData["msg_id"]?.String ?: ""
            val msgTimestamp = eventData["msg_timestamp"]?.Long ?: 0L
            val nonce = eventData["nonce"]?.String ?: ""
            val verifyToken = eventData["verify_token"]?.String
            val extra = eventData["extra"] ?: JsonNull

            // 系统事件 (type = 255)
            if (messageType == MessageType.SYSTEM) {
                val context = SystemEventContext(
                    channelType = channelPrivacyType,
                    targetId = targetId,
                    authorId = authorId,
                    content = content,
                    msgId = msgId,
                    msgTimestamp = msgTimestamp,
                    nonce = nonce,
                )
                return decodeSystemEvent(context, verifyToken, sn, extra, json)
            }

            // 用户消息 (type != 255)
            val extraObj = extra as? JsonObject
            val author: IUser? = extraObj?.get("author")?.let {
                json.decodeFromJsonElement(IUser.serializer(), it)
            }
            val guildId = extraObj?.get("guild_id")?.String ?: ""
            val channelName = extraObj?.get("channel_name")?.String ?: ""
            val mention = extraObj?.get("mention")?.let {
                json.decodeFromJsonElement<List<String>>(it)
            } ?: emptyList()
            val mentionAll = extraObj?.get("mention_all")?.jsonPrimitive?.booleanOrNull ?: false
            val mentionRoles = extraObj?.get("mention_roles")?.let {
                json.decodeFromJsonElement<List<Int>>(it)
            } ?: emptyList()
            val mentionHere = extraObj?.get("mention_here")?.jsonPrimitive?.booleanOrNull ?: false
            val kmarkdown = extraObj?.get("kmarkdown")?.let {
                json.decodeFromJsonElement<KMarkdown>(it)
            }
            val attachment = extraObj?.get("attachments")?.let {
                json.decodeFromJsonElement<Attachment>(it)
            }

            return UserMessageEvent(
                channelType = channelPrivacyType,
                messageType = messageType,
                targetId = targetId,
                authorId = authorId,
                content = content,
                msgId = msgId,
                msgTimestamp = msgTimestamp,
                nonce = nonce,
                guildId = guildId,
                channelName = channelName,
                mention = mention,
                mentionAll = mentionAll,
                mentionRoles = mentionRoles,
                mentionHere = mentionHere,
                author = author,
                kmarkdown = kmarkdown,
                attachment = attachment,
                extra = extra,
                verifyToken = verifyToken,
                sn = sn,
            )
        }

        private fun decodeSystemEvent(
            context: SystemEventContext,
            verifyToken: String?,
            sn: Int?,
            extra: JsonElement,
            json: Json,
        ): SystemEvent {
            val extraType = extra["type"]?.String ?: "unknown"
            val body = extra["body"] ?: JsonNull
            val systemEventType = SystemEventType.fromValue(extraType)

            return when (systemEventType) {
                // 频道反应事件
                SystemEventType.ADDED_REACTION -> ReactionAddedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.DELETED_REACTION -> ReactionDeletedEvent(json.decodeFromJsonElement(body), context, sn)

                // 消息操作事件
                SystemEventType.UPDATED_MESSAGE -> MessageUpdatedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.EMBEDS_APPEND -> EmbedsAppendEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.DELETED_MESSAGE -> MessageDeletedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.PINNED_MESSAGE -> MessagePinnedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.UNPINNED_MESSAGE -> MessageUnpinnedEvent(json.decodeFromJsonElement(body), context, sn)

                // 频道事件
                SystemEventType.ADDED_CHANNEL -> ChannelAddedEvent(json.decodeFromJsonElement(Channel.serializer(), body), context, sn)
                SystemEventType.UPDATED_CHANNEL -> ChannelUpdatedEvent(json.decodeFromJsonElement(Channel.serializer(), body), context, sn)
                SystemEventType.UPDATED_SERVER_TYPE -> ChannelServerTypeUpdatedEvent(json.decodeFromJsonElement(Channel.serializer(), body), context, sn)
                SystemEventType.SORT_CHANNEL -> ChannelSortedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.DELETED_CHANNEL -> ChannelDeletedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.BATCH_ADDED_CHANNEL -> ChannelBatchAddedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.BATCH_UPDATED_CHANNEL -> ChannelBatchUpdatedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.BATCH_DELETED_CHANNEL -> ChannelBatchDeletedEvent(json.decodeFromJsonElement(body), context, sn)

                // 服务器成员事件
                SystemEventType.JOINED_GUILD -> MemberJoinedGuildEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.EXITED_GUILD -> MemberExitedGuildEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.UPDATED_GUILD_MEMBER -> MemberUpdatedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.GUILD_MEMBER_ONLINE -> MemberOnlineEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.GUILD_MEMBER_OFFLINE -> MemberOfflineEvent(json.decodeFromJsonElement(body), context, sn)

                // 服务器事件
                SystemEventType.UPDATED_GUILD -> {
                    when (context.channelType) {
                        ChannelPrivacyType.GROUP -> GuildUpdatedEvent(json.decodeFromJsonElement(body), context, sn)
                        ChannelPrivacyType.PERSON -> GuildSelfUpdatedEvent(json.decodeFromJsonElement(body), context, sn)
                        else -> UnknownSystemEvent(extraType, body, context, sn)
                    }
                }
                SystemEventType.DELETED_GUILD -> GuildDeletedEvent(json.decodeFromJsonElement(body), context, sn)

                // 角色事件
                SystemEventType.ADDED_ROLE -> RoleAddedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.DELETED_ROLE -> RoleDeletedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.UPDATED_ROLE -> RoleUpdatedEvent(json.decodeFromJsonElement(body), context, sn)

                // 封禁事件
                SystemEventType.ADDED_BLOCK_LIST -> BlockListAddedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.DELETED_BLOCK_LIST -> BlockListDeletedEvent(json.decodeFromJsonElement(body), context, sn)

                // 表情事件
                SystemEventType.ADDED_EMOJI -> EmojiAddedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.REMOVED_EMOJI -> EmojiRemovedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.DELETED_EMOJI -> EmojiDeletedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.UPDATED_EMOJI -> EmojiUpdatedEvent(json.decodeFromJsonElement(body), context, sn)

                // 语音事件
                SystemEventType.JOINED_CHANNEL -> UserJoinedVoiceChannelEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.EXITED_CHANNEL -> UserExitedVoiceChannelEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.LIVE_STATUS_CHANGED -> LiveStatusChangedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.ADD_GUILD_MUTE -> GuildMuteAddedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.DELETE_GUILD_MUTE -> GuildMuteDeletedEvent(json.decodeFromJsonElement(body), context, sn)

                // 私聊事件
                SystemEventType.UPDATED_PRIVATE_MESSAGE -> DirectMessageUpdatedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.DELETED_PRIVATE_MESSAGE -> DirectMessageDeletedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.PRIVATE_ADDED_REACTION -> DirectReactionAddedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.PRIVATE_DELETED_REACTION -> DirectReactionDeletedEvent(json.decodeFromJsonElement(body), context, sn)

                // 用户事件
                SystemEventType.USER_UPDATED -> UserUpdatedEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.SELF_JOINED_GUILD -> SelfJoinedGuildEvent(json.decodeFromJsonElement(body), context, sn)
                SystemEventType.SELF_EXITED_GUILD -> SelfExitedGuildEvent(json.decodeFromJsonElement(body), context, sn)

                // 交互事件
                SystemEventType.MESSAGE_BTN_CLICK -> ButtonClickedEvent(json.decodeFromJsonElement(body), context, sn)

                // 未知事件 - 保持原始 JSON
                SystemEventType.UNKNOWN -> UnknownSystemEvent(extraType, body, context, sn)
            }
        }
    }
}

// ============ MessageEvent 层级 ============

sealed class MessageEvent : Event() {
    abstract val verifyToken: String?
}

data class ChallengeEvent(
    val challenge: String,
    override val verifyToken: String? = null,
    override val sn: Int? = null,
) : MessageEvent()

data class UserMessageEvent(
    val channelType: ChannelPrivacyType,
    val messageType: MessageType,
    val targetId: String,
    val authorId: String,
    val content: String,
    val msgId: String,
    val msgTimestamp: Long,
    val nonce: String,
    val guildId: String,
    val channelName: String,
    val mention: List<String>,
    val mentionAll: Boolean,
    val mentionRoles: List<Int>,
    val mentionHere: Boolean,
    val author: IUser?,
    val kmarkdown: KMarkdown?,
    val attachment: Attachment?,
    val extra: JsonElement,
    override val verifyToken: String? = null,
    override val sn: Int? = null,
) : MessageEvent()

// ============ SystemEvent 层级 ============

/** 系统事件公共上下文 */
data class SystemEventContext(
    val channelType: ChannelPrivacyType,
    val targetId: String,
    val authorId: String,
    val content: String,
    val msgId: String,
    val msgTimestamp: Long,
    val nonce: String,
)

/** 系统事件基类 */
sealed class SystemEvent : MessageEvent() {
    abstract val context: SystemEventContext
    override val verifyToken: String? get() = null
}

// ============ 频道事件 ============

data class ReactionAddedEvent(val data: ReactionEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class ReactionDeletedEvent(val data: ReactionEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class MessageUpdatedEvent(val data: MessageUpdatedEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class EmbedsAppendEvent(val data: EmbedsAppendEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class MessageDeletedEvent(val data: MessageDeletedEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class MessagePinnedEvent(val data: MessagePinEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class MessageUnpinnedEvent(val data: MessagePinEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class ChannelAddedEvent(val data: IChannel, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class ChannelUpdatedEvent(val data: IChannel, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class ChannelServerTypeUpdatedEvent(val data: IChannel, override val context: SystemEventContext, override val sn: Int?) :
    SystemEvent()
data class ChannelSortedEvent(val data: ChannelSortEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class ChannelDeletedEvent(val data: ChannelDeletedEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class ChannelBatchAddedEvent(val data: List<Channel>, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class ChannelBatchUpdatedEvent(val data: ChannelBatchUpdateEventData, override val context: SystemEventContext, override val sn: Int?) :
    SystemEvent()
data class ChannelBatchDeletedEvent(val data: List<ChannelBatchDeleteEventItemData>, override val context: SystemEventContext, override val sn: Int?) :
    SystemEvent()

// ============ 服务器成员事件 ============

data class MemberJoinedGuildEvent(val data: MemberJoinedEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class MemberExitedGuildEvent(val data: MemberExitedEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class MemberUpdatedEvent(val data: MemberUpdatedEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class MemberOnlineEvent(val data: MemberOnlineStatusEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class MemberOfflineEvent(val data: MemberOnlineStatusEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()

// ============ 服务器事件 ============

data class GuildUpdatedEvent(val data: GuildEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class GuildDeletedEvent(val data: GuildEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class GuildSelfUpdatedEvent(val data: GuildSelfUpdateEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class BlockListAddedEvent(val data: BlockListAddedEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class BlockListDeletedEvent(val data: BlockListDeletedEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class EmojiAddedEvent(val data: EmojiEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class EmojiRemovedEvent(val data: EmojiEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class EmojiDeletedEvent(val data: EmojiEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class EmojiUpdatedEvent(val data: EmojiEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()

// ============ 角色事件 ============

data class RoleAddedEvent(val data: RoleEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class RoleDeletedEvent(val data: RoleEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class RoleUpdatedEvent(val data: RoleEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()

// ============ 私聊事件 ============

data class DirectMessageUpdatedEvent(val data: DirectMessageUpdatedEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class DirectMessageDeletedEvent(val data: DirectMessageDeletedEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class DirectReactionAddedEvent(val data: DirectReactionEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class DirectReactionDeletedEvent(val data: DirectReactionEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()

// ============ 语音事件 ============

data class UserJoinedVoiceChannelEvent(val data: UserJoinedVoiceEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class UserExitedVoiceChannelEvent(val data: UserExitedVoiceEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class LiveStatusChangedEvent(val data: LiveStatusChangeEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class GuildMuteAddedEvent(val data: GuildMuteDeafEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class GuildMuteDeletedEvent(val data: GuildMuteDeafEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()

// ============ 用户事件 ============

data class UserUpdatedEvent(val data: UserUpdatedEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class SelfJoinedGuildEvent(val data: SelfJoinedGuildEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()
data class SelfExitedGuildEvent(val data: SelfExitedGuildEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()

// ============ 交互事件 ============

data class ButtonClickedEvent(val data: ButtonClickedEventData, override val context: SystemEventContext, override val sn: Int?) : SystemEvent()

// ============ 未知系统事件 ============

data class UnknownSystemEvent(
    val eventTypeName: String,
    val body: JsonElement,
    override val context: SystemEventContext,
    override val sn: Int?,
) : SystemEvent()

// ============ 信令事件 ============

data class HandshakeAckEvent(
    override val sn: Int? = null,
    val data: JsonElement? = null,
) : Event()

data class PongEvent(
    override val sn: Int? = null,
) : Event()

data class ReconnectEvent(
    override val sn: Int? = null,
    val data: JsonElement? = null,
) : Event()

/** 信令 5 携带的重连负载，包含服务端下发的错误码和错误信息 */
@Serializable
data class ReconnectPayload(
    val code: Int? = null,
    val err: String? = null,
)

data class ResumeAckEvent(
    override val sn: Int? = null,
    val data: JsonElement? = null,
) : Event()

data class UnknownEvent(
    val signalType: SignalType,
    override val sn: Int? = null,
    val data: JsonElement? = null,
) : Event()
