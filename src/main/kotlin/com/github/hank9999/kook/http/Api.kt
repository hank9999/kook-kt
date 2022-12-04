package com.github.hank9999.kook.http

import com.github.hank9999.kook.card.CardMessage
import com.github.hank9999.kook.http.types.Method
import com.github.hank9999.kook.types.types.MessageTypes
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import java.io.File

abstract class Api {
    abstract val method: Method
    abstract val bucket: String
    abstract val route: String
    abstract val postData: RequestBody
    abstract val params: Map<String, String>
    abstract val pageable: Boolean
    abstract val baseApiUrl: String?

    val mediaType = "application/json; charset=utf-8".toMediaType()
    val emptyPostData = "".toRequestBody(mediaType)
    val emptyParams = emptyMap<String, String>()

    abstract class Guild : Api() {
        class List(sort: String? = null) : Guild() {
            override val method = Method.GET
            override val bucket = "guild/list"
            override val route = "guild/list"
            override val postData = emptyPostData
            override val params = if (sort != null) mapOf("sort" to sort) else emptyParams
            override val pageable = true
            override val baseApiUrl = null
        }
        
        class View(guildId: String) : Guild() {
            override val method = Method.GET
            override val bucket = "guild/view"
            override val route = "guild/view"
            override val postData = emptyPostData
            override val params = mapOf("guild_id" to guildId)
            override val pageable = false
            override val baseApiUrl = null
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
            override val baseApiUrl = null
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
            override val baseApiUrl = null
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
            override val baseApiUrl = null
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
            override val baseApiUrl = null
        }
    }
    
    abstract class GuildMute : Api() {
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
            override val baseApiUrl = null
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
            override val baseApiUrl = null
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
            override val baseApiUrl = null
        }
    }

    abstract class GuildBoost : Api() {
        class History(guildId: String, startTime: Long? = null, endTime: Long? = null) : GuildBoost() {
            override val method = Method.GET
            override val bucket = "guild-boost/history"
            override val route = "guild-boost/history"
            override val postData = emptyPostData
            override val params = mutableMapOf<String, String>().apply {
                this["guild_id"] = guildId
                startTime?.let { this["start_time"] = startTime.toString() }
                endTime?.let { this["end_time"] = endTime.toString() }
            }
            override val pageable = false
            override val baseApiUrl = null
        }
    }

    abstract class Channel : Api() {
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
            override val baseApiUrl = null
        }
        
        class View(targetId: String) : Channel() {
            override val method = Method.GET
            override val bucket = "channel/view"
            override val route = "channel/view"
            override val postData = emptyPostData
            override val params = mapOf("target_id" to targetId)
            override val pageable = false
            override val baseApiUrl = null
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
            override val baseApiUrl = null
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
            override val baseApiUrl = null
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
            override val baseApiUrl = null
        }

        class UserList(channelId: String) : Channel() {
            override val method = Method.GET
            override val bucket = "channel/user-list"
            override val route = "channel/user-list"
            override val postData = emptyPostData
            override val params = mapOf("channel_id" to channelId)
            override val pageable = false
            override val baseApiUrl = null
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
            override val baseApiUrl = null
        }
    }
    
    abstract class ChannelRole : Api() {
        class Index(channelId: String) : ChannelRole() {
            override val method = Method.GET
            override val bucket = "channel-role/index"
            override val route = "channel-role/index"
            override val postData = emptyPostData
            override val params = mapOf("channel_id" to channelId)
            override val pageable = false
            override val baseApiUrl = null
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
            override val baseApiUrl = null
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
            override val baseApiUrl = null
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
            override val baseApiUrl = null
        }
    }
    
    abstract class Message : Api() {
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
            override val baseApiUrl = null
        }
        
        class View(msgId: String) : Message() {
            override val method = Method.GET
            override val bucket = "message/view"
            override val route = "message/view"
            override val postData = emptyPostData
            override val params = mapOf("msg_id" to msgId)
            override val pageable = false
            override val baseApiUrl = null
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
            override val baseApiUrl = null
        }
        
        class Update(msgId: String, content: Any, quote: String? = null, tempTargetId: String? = null) : Message() {
            override val method = Method.POST
            override val bucket = "message/update"
            override val route = "message/update"
            override val postData = buildJsonObject {
                put("msg_id", msgId)
                put("content", content.toString())
                quote?.let { put("quote", quote) }
                tempTargetId?.let { put("temp_target_id", tempTargetId) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
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
            override val baseApiUrl = null
        }
        
        class ReactionList(msgId: String, emoji: String) : Message() {
            override val method = Method.GET
            override val bucket = "message/reaction-list"
            override val route = "message/reaction-list"
            override val postData = emptyPostData
            override val params = mapOf("msg_id" to msgId, "emoji" to emoji)
            override val pageable = false
            override val baseApiUrl = null
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
            override val baseApiUrl = null
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
            override val baseApiUrl = null
        }
    }
    
    abstract class ChannelUser : Api() {
        class GetJoinedChannel(guildId: String, userId: String) : ChannelUser() {
            override val method = Method.GET
            override val bucket = "channel-user/get-joined-channel"
            override val route = "channel-user/get-joined-channel"
            override val postData = emptyPostData
            override val params = mapOf("guild_id" to guildId, "user_id" to userId)
            override val pageable = true
            override val baseApiUrl = null
        }
    }
    
    abstract class UserChat : Api() {
        class List : UserChat() {
            override val method = Method.GET
            override val bucket = "user-chat/list"
            override val route = "user-chat/list"
            override val postData = emptyPostData
            override val params = emptyParams
            override val pageable = true
            override val baseApiUrl = null
        }
        
        class Create(targetId: String) : UserChat() {
            override val method = Method.POST
            override val bucket = "user-chat/create"
            override val route = "user-chat/create"
            override val postData = buildJsonObject {
                put("target_id", targetId)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class Delete(chatCode: String) : UserChat() {
            override val method = Method.POST
            override val bucket = "user-chat/delete"
            override val route = "user-chat/delete"
            override val postData = buildJsonObject {
                put("chat_code", chatCode)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
    }
    
    abstract class DirectMessage : Api() {
        class List(chatCode: String? = null, targetId: String? = null, msgId: String? = null, flag: String? = null) : DirectMessage() {
            override val method = Method.GET
            override val bucket = "direct-message/list"
            override val route = "direct-message/list"
            override val postData = emptyPostData
            override val params = mutableMapOf<String, String>().apply {
                require(chatCode != null || targetId != null)
                chatCode?.let { this["chat_code"] = chatCode }
                targetId?.let { this["target_id"] = targetId }
                msgId?.let { this["msg_id"] = msgId }
                flag?.let { this["flag"] = flag }
            }
            override val pageable = true
            override val baseApiUrl = null
        }
        
        class Create(chatCode: String? = null, targetId: String? = null, type: Int? = null, content: String, quote: String? = null, nonce: String? = null) : DirectMessage() {
            override val method = Method.POST
            override val bucket = "direct-message/create"
            override val route = "direct-message/create"
            override val postData = buildJsonObject {
                require(chatCode != null || targetId != null)
                chatCode?.let { put("chat_code", chatCode) }
                targetId?.let { put("target_id", targetId) }
                type?.let { put("target_id", type) }
                put("content", content)
                quote?.let { put("quote", quote) }
                nonce?.let { put("nonce", nonce) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class Update(msgId: String? = null, content: String, quote: String? = null) : DirectMessage() {
            override val method = Method.POST
            override val bucket = "direct-message/update"
            override val route = "direct-message/update"
            override val postData = buildJsonObject {
                msgId?.let { put("msg_id", msgId) }
                put("content", content)
                quote?.let { put("quote", quote) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class Delete(msgId: String? = null) : DirectMessage() {
            override val method = Method.POST
            override val bucket = "direct-message/delete"
            override val route = "direct-message/delete"
            override val postData = buildJsonObject {
                msgId?.let { put("msg_id", msgId) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class ReactionList(msgId: String, emoji: String? = null) : DirectMessage() {
            override val method = Method.GET
            override val bucket = "direct-message/reaction-list"
            override val route = "direct-message/reaction-list"
            override val postData = emptyPostData
            override val params = mutableMapOf("msg_id" to msgId).apply {
                emoji?.let { this["emoji"] = emoji }
            }
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class AddReaction(msgId: String, emoji: String) : DirectMessage() {
            override val method = Method.POST
            override val bucket = "direct-message/add-reaction"
            override val route = "direct-message/add-reaction"
            override val postData = buildJsonObject {
                put("msg_id", msgId)
                put("emoji", emoji)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class DeleteReaction(msgId: String, emoji: String, userId: String?) : DirectMessage() {
            override val method = Method.POST
            override val bucket = "direct-message/delete-reaction"
            override val route = "direct-message/delete-reaction"
            override val postData = buildJsonObject {
                put("msg_id", msgId)
                put("emoji", emoji)
                userId?.let { put("user_id", userId) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
    }
    
    abstract class Gateway : Api() {
        class Index(compress: Int? = null) : Gateway() {
            override val method = Method.GET
            override val bucket = "gateway/index"
            override val route = "gateway/index"
            override val postData = emptyPostData
            override val params = mutableMapOf<String, String>().apply {
                compress?.let { this["compress"] = compress.toString() }
            }
            override val pageable = false
            override val baseApiUrl = null
        }
    }
    
    abstract class User : Api() {
        class Me : User() {
            override val method = Method.GET
            override val bucket = "user/me"
            override val route = "user/me"
            override val postData = emptyPostData
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class View(userId: String, guildId: String? = null) : User() {
            override val method = Method.GET
            override val bucket = "user/view"
            override val route = "user/view"
            override val postData = emptyPostData
            override val params = mutableMapOf<String, String>().apply {
                this["user_id"] = userId
                guildId?.let { this["guild_id"] = guildId }
            }
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class Offline : User() {
            override val method = Method.POST
            override val bucket = "user/offline"
            override val route = "user/offline"
            override val postData = emptyPostData
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
    }
    
    abstract class Asset : Api() {
        class Create(file: File) : Asset() {
            override val method = Method.POST
            override val bucket = "asset/create"
            override val route = "asset/create"
            override val postData = MultipartBody.Builder().apply {
                this.setType(MultipartBody.FORM)
                this.addFormDataPart("file", file.name, file.asRequestBody())
            }.build()
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class CreateWithBytes(file: ByteArray, fileName: String = "default") : Asset() {
            override val method = Method.POST
            override val bucket = "asset/create"
            override val route = "asset/create"
            override val postData = MultipartBody.Builder().apply {
                this.setType(MultipartBody.FORM)
                this.addFormDataPart("file", fileName, object : RequestBody() {
                    override fun contentType() = null
                    override fun contentLength() = file.size.toLong()
                    override fun writeTo(sink: BufferedSink) {
                        sink.write(file)
                    }
                })
            }.build()
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
    }
    
    abstract class GuildRole : Api() {
        class List(guildId: String) : GuildRole() {
            override val method = Method.GET
            override val bucket = "guild-role/list"
            override val route = "guild-role/list"
            override val postData = emptyPostData
            override val params = mapOf("guild_id" to guildId)
            override val pageable = true
            override val baseApiUrl = null
        }
        
        class Create(guildId: String, name: String? = null) : GuildRole() {
            override val method = Method.POST
            override val bucket = "guild-role/create"
            override val route = "guild-role/create"
            override val postData = buildJsonObject {
                put("guild_id", guildId)
                name?.let { put("name", name) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class Update(guildId: String, roleId: Int, name: String? = null, color: Int? = null, hoist: Int? = null, mentionable: Int? = null, permissions: Int? = null) : GuildRole() {
            override val method = Method.POST
            override val bucket = "guild-role/update"
            override val route = "guild-role/update"
            override val postData = buildJsonObject {
                put("guild_id", guildId)
                put("role_id", roleId)
                name?.let { put("name", name) }
                color?.let { put("color", color) }
                hoist?.let { require(hoist in 0..1); put("hoist", hoist) }
                mentionable?.let { require(mentionable in 0..1); put("mentionable", mentionable) }
                permissions?.let { put("permissions", permissions) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class Delete(guildId: String, roleId: Int) : GuildRole() {
            override val method = Method.POST
            override val bucket = "guild-role/delete"
            override val route = "guild-role/delete"
            override val postData = buildJsonObject {
                put("guild_id", guildId)
                put("role_id", roleId)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class Grant(guildId: String, userId: String, roleId: Int) : GuildRole() {
            override val method = Method.POST
            override val bucket = "guild-role/grant"
            override val route = "guild-role/grant"
            override val postData = buildJsonObject {
                put("guild_id", guildId)
                put("user_id", userId)
                put("role_id", roleId)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class Revoke(guildId: String, userId: String, roleId: Int) : GuildRole() {
            override val method = Method.POST
            override val bucket = "guild-role/revoke"
            override val route = "guild-role/revoke"
            override val postData = buildJsonObject {
                put("guild_id", guildId)
                put("user_id", userId)
                put("role_id", roleId)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
    }
    
    abstract class Intimacy : Api() {
        class Index(userId: String) : Intimacy() {
            override val method = Method.GET
            override val bucket = "intimacy/index"
            override val route = "intimacy/index"
            override val postData = emptyPostData
            override val params = mapOf("user_id" to userId)
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class Update(userId: String, score: Int? = null, socialInfo: String? = null, imgId: String? = null) : Intimacy() {
            override val method = Method.POST
            override val bucket = "intimacy/update"
            override val route = "intimacy/update"
            override val postData = buildJsonObject {
                put("user_id", userId)
                score?.let { put("score", score) }
                socialInfo?.let { put("social_info", socialInfo) }
                imgId?.let { put("img_id", imgId) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
    }
    
    abstract class GuildEmoji : Api() {
        class List(guildId: String) : GuildEmoji() {
            override val method = Method.GET
            override val bucket = "intimacy/index"
            override val route = "intimacy/index"
            override val postData = emptyPostData
            override val params = mapOf("guild_id" to guildId)
            override val pageable = true
            override val baseApiUrl = null
        }
        
        class Create(guildId: String, name: String? = null, emoji: File) : GuildEmoji() {
            override val method = Method.POST
            override val bucket = "guild-emoji/create"
            override val route = "guild-emoji/create"
            override val postData = MultipartBody.Builder().apply {
                this.setType(MultipartBody.FORM)
                this.addFormDataPart("guild_id", guildId)
                name?.let { this.addFormDataPart("name", name) }
                this.addFormDataPart("emoji", emoji.name, emoji.asRequestBody())
            }.build()
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class CreateWithBytes(guildId: String, name: String? = null, emoji: ByteArray, fileName: String = "default") : GuildEmoji() {
            override val method = Method.POST
            override val bucket = "guild-emoji/create"
            override val route = "guild-emoji/create"
            override val postData = MultipartBody.Builder().apply {
                this.setType(MultipartBody.FORM)
                this.addFormDataPart("guild_id", guildId)
                name?.let { this.addFormDataPart("name", name) }
                this.addFormDataPart("emoji", fileName, object : RequestBody() {
                    override fun contentType() = null
                    override fun contentLength() = emoji.size.toLong()
                    override fun writeTo(sink: BufferedSink) {
                        sink.write(emoji)
                    }
                })
            }.build()
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class Update(id: String, name: String? = null) : GuildEmoji() {
            override val method = Method.POST
            override val bucket = "guild-emoji/update"
            override val route = "guild-emoji/update"
            override val postData = buildJsonObject {
                put("id", id)
                name?.let { put("name", name) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
        
        class Delete(id: String) : GuildEmoji() {
            override val method = Method.POST
            override val bucket = "guild-emoji/delete"
            override val route = "guild-emoji/delete"
            override val postData = buildJsonObject {
                put("id", id)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
    }
    
    abstract class Invite : Api() {
        class List(guildId: String? = null, channelId: String? = null) : Invite() {
            override val method = Method.GET
            override val bucket = "invite/list"
            override val route = "invite/list"
            override val postData = emptyPostData
            override val params = mutableMapOf<String, String>().apply {
                require( guildId != null || channelId != null )
                guildId?.let { this["guild_id"] = guildId }
                channelId?.let { this["channel_id"] = channelId }
            }
            override val pageable = true
            override val baseApiUrl = null
        }

        class Create(guildId: String? = null, channelId: String? = null, duration: Int? = null, settingTimes: Int? = null) : Invite() {
            override val method = Method.POST
            override val bucket = "invite/create"
            override val route = "invite/create"
            override val postData = buildJsonObject {
                require( guildId != null || channelId != null )
                guildId?.let { put("guild_id", guildId) }
                channelId?.let { put("channel_id", channelId) }
                duration?.let { put("duration", duration) }
                settingTimes?.let { put("setting_times", settingTimes) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }

        class Delete(urlCode: String, guildId: String? = null, channelId: String? = null) : Invite() {
            override val method = Method.POST
            override val bucket = "invite/delete"
            override val route = "invite/delete"
            override val postData = buildJsonObject {
                put("url_code", urlCode)
                guildId?.let { put("guild_id", guildId) }
                channelId?.let { put("channel_id", channelId) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
    }

    abstract class Blacklist : Api() {
        class List(guildId: String) : Blacklist() {
            override val method = Method.GET
            override val bucket = "blacklist/list"
            override val route = "blacklist/list"
            override val postData = emptyPostData
            override val params = mapOf("guild_id" to guildId)
            override val pageable = true
            override val baseApiUrl = null
        }

        class Create(guildId: String, targetId: String, remark: String? = null, delMsgDays: Int? = null) : Blacklist() {
            override val method = Method.POST
            override val bucket = "blacklist/create"
            override val route = "blacklist/create"
            override val postData = buildJsonObject {
                put("guild_id", guildId)
                put("target_id", targetId)
                remark?.let { put("remark", remark) }
                delMsgDays?.let { put("del_msg_days", delMsgDays) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }

        class Delete(guildId: String, targetId: String) : Blacklist() {
            override val method = Method.POST
            override val bucket = "blacklist/delete"
            override val route = "blacklist/delete"
            override val postData = buildJsonObject {
                put("guild_id", guildId)
                put("target_id", targetId)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
    }

    abstract class Badge : Api() {
        class Guild(guildId: String, style: Int? = null) : Badge() {
            override val method = Method.GET
            override val bucket = "badge/guild"
            override val route = "badge/guild"
            override val postData = emptyPostData
            override val params = mutableMapOf<String, String>().apply {
                this["guild_id"] = guildId
                style?.let { require( style in 0..2 ); this["style"] = style.toString() }
            }
            override val pageable = false
            override val baseApiUrl = null
        }
    }

    abstract class Game : Api() {
        class List : Game() {
            override val method = Method.GET
            override val bucket = "game"
            override val route = "game"
            override val postData = emptyPostData
            override val params = emptyParams
            override val pageable = true
            override val baseApiUrl = null
        }

        class Create(name: String, icon: String? = null) : Game() {
            override val method = Method.POST
            override val bucket = "game/create"
            override val route = "game/create"
            override val postData = buildJsonObject {
                put("name", name)
                icon?.let { put("icon", icon) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }

        class Update(id: Int, name: String? = null, icon: String? = null) : Game() {
            override val method = Method.POST
            override val bucket = "game/create"
            override val route = "game/create"
            override val postData = buildJsonObject {
                put("id", id)
                name?.let { put("name", name) }
                icon?.let { put("icon", icon) }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }

        class Delete(id: Int) : Game() {
            override val method = Method.POST
            override val bucket = "game/update"
            override val route = "game/update"
            override val postData = buildJsonObject {
                put("id", id)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }

        class Activity(dataType: Int, id: Int? = null, software: String? = null, singer: String? = null, musicName: String? = null) : Game() {
            override val method = Method.POST
            override val bucket = "game/activity"
            override val route = "game/activity"
            override val postData = buildJsonObject {
                require( dataType in 1..2 )
                put("data_type", dataType)
                if (dataType == 1) {
                    require( id != null )
                    put("id", id)
                } else if (dataType == 2) {
                    require( singer != null && musicName != null)
                    software?.let { put("software", software) }
                    put("singer", singer)
                    put("music_name", musicName)
                }
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }

        class DeleteActivity(dataType: Int) : Game() {
            override val method = Method.POST
            override val bucket = "game/delete-activity"
            override val route = "game/delete-activity"
            override val postData = buildJsonObject {
                require( dataType in 1..2 )
                put("data_type", dataType)
            }.toString().toRequestBody(mediaType)
            override val params = emptyParams
            override val pageable = false
            override val baseApiUrl = null
        }
    }

     abstract class Oauth2 : Api() {
         class Token(grantType: String, clientId: String, clientSecret: String, code: String, redirectUri: String) : Oauth2() {
             override val method = Method.POST
             override val bucket = "oauth2/token"
             override val route = "oauth2/token"
             override val postData = buildJsonObject {
                 put("grant_type", grantType)
                 put("client_id", clientId)
                 put("client_secret", clientSecret)
                 put("code", code)
                 put("redirect_uri", redirectUri)
             }.toString().toRequestBody(mediaType)
             override val params = emptyParams
             override val pageable = false
                 override val baseApiUrl = "https://www.kookapp.cn/api"
         }
     }
}

