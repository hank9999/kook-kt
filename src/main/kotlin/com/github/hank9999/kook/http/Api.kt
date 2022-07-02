package com.github.hank9999.kook.http

import com.github.hank9999.kook.card.CardMessage
import com.github.hank9999.kook.http.types.Method
import com.github.hank9999.kook.types.types.MessageTypes
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

abstract class Api {
    abstract val method: Method
    abstract val bucket: String
    abstract val route: String
    abstract val postData: RequestBody
    abstract val params: Map<String, String>
    abstract val pageable: Boolean

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val emptyPostData = "".toRequestBody(mediaType)
    val emptyParams = emptyMap<String, String>()

    abstract class Guild: Api() {
        class List(sort: String? = null) : Guild() {
            override val method = Method.GET
            override val bucket = "guild/list"
            override val route = "guild/list"
            override val postData = emptyPostData
            override val params = if (sort != null) mapOf("sort" to sort) else emptyParams
            override val pageable = true
        }
        class View(guildId: String) : Guild() {
            override val method = Method.GET
            override val bucket = "guild/view"
            override val route = "guild/view"
            override val postData = emptyPostData
            override val params = mapOf("guild_id" to guildId)
            override val pageable = false
        }
        class UserList(guildId: String, channelId: String? = null, search: String? = null, roleId: Int? = null, mobileVerified: Int? = null, activeTime: Int? = null, joinedAt: Int? = null) : Guild() {
            override val method = Method.GET
            override val bucket = "guild/user-list"
            override val route = "guild/user-list"
            override val postData = emptyPostData
            override val params = mutableMapOf<String, String>().apply {
                this["guild_id"] = guildId
                channelId?.let { this["channel_id"] = channelId }
                search?.let { this["search"] = search }
                roleId?.let { this["role_id"] = roleId.toString() }
                mobileVerified?.let { require(mobileVerified in 0..1); this["mobile_verified"] = mobileVerified.toString() }
                activeTime?.let { require(activeTime in 0..1); this["active_time"] = activeTime.toString() }
                joinedAt?.let { require(joinedAt in 0..1); this["joined_at"] = joinedAt.toString()}
            }
            override val pageable = true
        }
        class Nickname(guildId: String, nickname: String? = null, userId: String? = null) : Guild() {
            override val method = Method.POST
            override val bucket = "guild/nickname"
            override val route = "guild/nickname"
            override val postData = buildJsonObject {
                put("guild_id", guildId)
                put("nickname", nickname ?: "")
                userId?.let { put("user_id", userId) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
        class Leave(guildId: String) : Guild() {
            override val method = Method.POST
            override val bucket = "guild/leave"
            override val route = "guild/leave"
            override val postData = buildJsonObject {
                put("guild_id", guildId)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
        class KickOut(guildId: String, targetId: String) : Guild() {
            override val method = Method.POST
            override val bucket = "guild/nickname"
            override val route = "guild/nickname"
            override val postData = buildJsonObject {
                put("guild_id", guildId)
                put("target_id", targetId)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
    }
    abstract class GuildMute: Api() {
        class List(guildId: String, returnType: String? = null) : GuildMute() {
            override val method = Method.GET
            override val bucket = "guild-mute/list"
            override val route = "guild-mute/list"
            override val postData = emptyPostData
            override val params = mutableMapOf<String, String>().apply {
                this["guild_id"] = guildId
                returnType?.let { this["return_type"] = returnType }
            }
            override val pageable = false
        }
        class Create(guildId: String, userId: String, type: Int) : GuildMute() {
            override val method = Method.POST
            override val bucket = "guild-mute/create"
            override val route = "guild-mute/create"
            override val postData = buildJsonObject {
                put("guild_id", guildId)
                put("user_id", userId)
                put("type", type)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
        class Delete(guildId: String, userId: String, type: Int) : GuildMute() {
            override val method = Method.POST
            override val bucket = "guild-mute/delete"
            override val route = "guild-mute/delete"
            override val postData = buildJsonObject {
                put("guild_id", guildId)
                put("user_id", userId)
                put("type", type)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
    }
    abstract class Channel: Api() {
        class List(guildId: String, type: Int? = null) : Channel() {
            override val method = Method.GET
            override val bucket = "channel/list"
            override val route = "channel/list"
            override val postData = emptyPostData
            override val params = mutableMapOf<String, String>().apply {
                this["guild_id"] = guildId
                type?.let { this["type"] = type.toString() }
            }
            override val pageable = true
        }
        class View(targetId: String) : Channel() {
            override val method = Method.GET
            override val bucket = "channel/view"
            override val route = "channel/view"
            override val postData = emptyPostData
            override val params = mapOf("target_id" to targetId)
            override val pageable = false
        }
        class Create(guildId: String, name: String, type: Int? = null, parentId: String? = null, limitAmount: Int? = null, voiceQuality: Int? = null) : Channel() {
            override val method = Method.POST
            override val bucket = "channel/create"
            override val route = "channel/create"
            override val postData = buildJsonObject {
                put("guild_id", guildId)
                put("name", name)
                type?.let { put("type", type) }
                parentId?.let { put("parent_id", parentId) }
                limitAmount?.let { put("limit_amount", limitAmount) }
                voiceQuality?.let { put("voice_quality", voiceQuality) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
        class Update(channelId: String, name: String? = null, topic: String? = null, slowMode: Int? = null) : Channel() {
            override val method = Method.POST
            override val bucket = "channel/update"
            override val route = "channel/update"
            override val postData = buildJsonObject {
                put("channel_id", channelId)
                name?.let { put("name", name) }
                topic?.let { put("topic", topic) }
                slowMode?.let { put("slow_mode", slowMode) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
        class Delete(channelId: String) : Channel() {
            override val method = Method.POST
            override val bucket = "channel/delete"
            override val route = "channel/delete"
            override val postData = buildJsonObject {
                put("channel_id", channelId)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
        class MoveUser(targetId: String, userIds: Array<String>) : Channel() {
            override val method = Method.POST
            override val bucket = "channel/delete"
            override val route = "channel/delete"
            override val postData = buildJsonObject {
                put("target_id", targetId)
                putJsonArray("user_ids") {
                    userIds.forEach {
                        add(it)
                    }
                }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
    }
    abstract class ChannelRole: Api() {
        class Index(channelId: String) : ChannelRole() {
            override val method = Method.GET
            override val bucket = "channel-role/index"
            override val route = "channel-role/index"
            override val postData = emptyPostData
            override val params = mapOf("channel_id" to channelId)
            override val pageable = false
        }
        class Create(channelId: String, type: String? = null, value: String? = null) : ChannelRole() {
            override val method = Method.POST
            override val bucket = "channel-role/create"
            override val route = "channel-role/create"
            override val postData = buildJsonObject {
                put("channel_id", channelId)
                type?.let { put("type", type) }
                value?.let { put("value", value) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
        class Update(channelId: String, type: String? = null, value: String? = null, allow: Int? = null, deny: Int? = null) : ChannelRole() {
            override val method = Method.POST
            override val bucket = "channel-role/update"
            override val route = "channel-role/update"
            override val postData = buildJsonObject {
                put("channel_id", channelId)
                type?.let { put("type", type) }
                value?.let { put("value", value) }
                allow?.let { put("allow", allow) }
                deny?.let { put("deny", deny) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
        class Delete(channelId: String, type: String? = null, value: String? = null) : ChannelRole() {
            override val method = Method.POST
            override val bucket = "channel-role/delete"
            override val route = "channel-role/delete"
            override val postData = buildJsonObject {
                put("channel_id", channelId)
                type?.let { put("type", type) }
                value?.let { put("value", value) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
    }
    abstract class Message: Api() {
        class List(targetId: String, msgId: String? = null, pin: Int? = null, flag: String? = null, pageSize: Int? = null) : Message() {
            override val method = Method.GET
            override val bucket = "message/list"
            override val route = "message/list"
            override val postData = emptyPostData
            override val params = mutableMapOf<String, String>().apply {
                this["target_id"] = targetId
                msgId?.let { this["msg_id"] = msgId }
                pin?.let { this["pin"] = pin.toString() }
                flag?.let { this["flag"] = flag }
                pageSize?.let { this["page_size"] = pageSize.toString() }
            }
            override val pageable = false
        }
        class View(msgId: String) : Message() {
            override val method = Method.GET
            override val bucket = "message/view"
            override val route = "message/view"
            override val postData = emptyPostData
            override val params = mapOf("msg_id" to msgId)
            override val pageable = false
        }
        class Create(targetId: String, content: Any, type: MessageTypes? = null, quote: String? = null, nonce: String? = null, tempTargetId: String? = null) : Message() {
            override val method = Method.POST
            override val bucket = "message/create"
            override val route = "message/create"
            override val postData = buildJsonObject {
                put("target_id", targetId)
                put("content", content.toString())
                when (content) {
                    is CardMessage -> put("type", MessageTypes.CARD.type)
                    else -> type?.let { put("type", type.type) }
                }
                quote?.let { put("quote", quote) }
                nonce?.let { put("nonce", nonce) }
                tempTargetId?.let { put("temp_target_id", tempTargetId) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
        class Update(msgId: String, content: Any, quote: String? = null, tempTargetId: String? = null) : Message() {
            override val method = Method.POST
            override val bucket = "message/create"
            override val route = "message/create"
            override val postData = buildJsonObject {
                put("msg_id", msgId)
                put("content", content.toString())
                quote?.let { put("quote", quote) }
                tempTargetId?.let { put("temp_target_id", tempTargetId) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
        class Delete(msgId: String) : Message() {
            override val method = Method.POST
            override val bucket = "message/delete"
            override val route = "message/delete"
            override val postData = buildJsonObject {
                put("msg_id", msgId)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
        class ReactionList(msgId: String, emoji: String) : Message() {
            override val method = Method.GET
            override val bucket = "message/reaction-list"
            override val route = "message/reaction-list"
            override val postData = emptyPostData
            override val params = mapOf("msg_id" to msgId, "emoji" to emoji)
            override val pageable = false
        }
        class AddReaction(msgId: String, emoji: String) : Message() {
            override val method = Method.POST
            override val bucket = "message/add-reaction"
            override val route = "message/add-reaction"
            override val postData = buildJsonObject {
                put("msg_id", msgId)
                put("emoji", emoji)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
        class DeleteReaction(msgId: String, emoji: String, userId: String?) : Message() {
            override val method = Method.POST
            override val bucket = "message/delete-reaction"
            override val route = "message/delete-reaction"
            override val postData = buildJsonObject {
                put("msg_id", msgId)
                put("emoji", emoji)
                userId?.let { put("user_id", userId) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
        }
    }
}

