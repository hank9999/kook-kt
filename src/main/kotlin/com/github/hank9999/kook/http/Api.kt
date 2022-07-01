package com.github.hank9999.kook.http

import com.github.hank9999.kook.http.types.Method
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import okhttp3.FormBody
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

    val emptyFormBody = FormBody.Builder().build()
    val emptyParams = emptyMap<String, String>()

    abstract class Guild: Api() {
        class List(sort: String? = null): Guild() {
            override val method = Method.GET
            override val bucket = "guild/list"
            override val route = "guild/list"
            override val postData = emptyFormBody
            override val params = if (sort != null) mapOf("sort" to sort) else emptyParams
            override val pageable = true
        }
        class View(guildId: String): Guild() {
            override val method = Method.GET
            override val bucket = "guild/view"
            override val route = "guild/view"
            override val postData = emptyFormBody
            override val params = mapOf("guild_id" to guildId)
            override val pageable = false
        }
        class UserList(guildId: String, channelId: String? = null, search: String? = null, roleId: Int? = null,
                       mobileVerified: Int? = null, activeTime: Int? = null, joinedAt: Int? = null): Guild() {
            override val method = Method.GET
            override val bucket = "guild/user-list"
            override val route = "guild/user-list"
            override val postData = emptyFormBody
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
        class Nickname(guildId: String, nickname: String? = null, userId: String? = null): Guild() {
            override val method = Method.POST
            override val bucket = "guild/nickname"
            override val route = "guild/nickname"
            override val postData = FormBody.Builder().apply {
                this.add("guild_id", guildId)
                this.add("nickname", nickname ?: "")
                userId?.let { this.add("user_id", userId) }
            }.build()
            override val params = emptyParams
            override val pageable = false
        }
        class Leave(guildId: String): Guild() {
            override val method = Method.POST
            override val bucket = "guild/leave"
            override val route = "guild/leave"
            override val postData = FormBody.Builder().add("guild_id", guildId).build()
            override val params = emptyParams
            override val pageable = false
        }
        class KickOut(guildId: String, targetId: String): Guild() {
            override val method = Method.POST
            override val bucket = "guild/nickname"
            override val route = "guild/nickname"
            override val postData = FormBody.Builder()
                .add("guild_id", guildId)
                .add("target_id", targetId)
                .build()
            override val params = emptyParams
            override val pageable = false
        }
    }
    abstract class GuildMute: Api() {
        class List(guildId: String, returnType: String? = null): GuildMute() {
            override val method = Method.GET
            override val bucket = "guild-mute/list"
            override val route = "guild-mute/list"
            override val postData = emptyFormBody
            override val params = mutableMapOf<String, String>().apply {
                this["guild_id"] = guildId
                returnType?.let { this["return_type"] = returnType }
            }
            override val pageable = false
        }
        class Create(guildId: String, userId: String, type: Int): GuildMute() {
            override val method = Method.POST
            override val bucket = "guild-mute/create"
            override val route = "guild-mute/create"
            override val postData = FormBody.Builder()
                .add("guild_id", guildId)
                .add("user_id", userId)
                .add("type", type.toString())
                .build()
            override val params = emptyParams
            override val pageable = false
        }
        class Delete(guildId: String, userId: String, type: Int): GuildMute() {
            override val method = Method.POST
            override val bucket = "guild-mute/delete"
            override val route = "guild-mute/delete"
            override val postData = FormBody.Builder()
                .add("guild_id", guildId)
                .add("user_id", userId)
                .add("type", type.toString())
                .build()
            override val params = emptyParams
            override val pageable = false
        }
    }
    abstract class Channel: Api() {
        class List(guildId: String, type: Int? = null): Channel() {
            override val method = Method.GET
            override val bucket = "channel/list"
            override val route = "channel/list"
            override val postData = emptyFormBody
            override val params = mutableMapOf<String, String>().apply {
                this["guild_id"] = guildId
                type?.let { this["type"] = type.toString() }
            }
            override val pageable = true
        }
        class View(targetId: String): Channel() {
            override val method = Method.GET
            override val bucket = "channel/view"
            override val route = "channel/view"
            override val postData = emptyFormBody
            override val params = mapOf("target_id" to targetId)
            override val pageable = false
        }
        class Create(guildId: String, name: String, type: Int? = null, parentId: String? = null,
                     limitAmount: Int? = null, voiceQuality: Int? = null): Channel() {
            override val method = Method.POST
            override val bucket = "channel/create"
            override val route = "channel/create"
            override val postData = FormBody.Builder().apply {
                this.add("guild_id", guildId)
                this.add("name", name)
                type?.let { this.add("type", type.toString()) }
                parentId?.let { this.add("parent_id", parentId) }
                limitAmount?.let { this.add("limit_amount", limitAmount.toString()) }
                voiceQuality?.let { this.add("voice_quality", voiceQuality.toString()) }
            }.build()
            override val params = emptyParams
            override val pageable = false
        }
        class Update(channelId: String, name: String? = null, topic: String? = null, slowMode: Int? = null): Channel() {
            override val method = Method.POST
            override val bucket = "channel/update"
            override val route = "channel/update"
            override val postData = FormBody.Builder().apply {
                this.add("channel_id", channelId)
                name?.let { this.add("name", name) }
                topic?.let { this.add("topic", topic) }
                slowMode?.let { this.add("slow_mode", slowMode.toString()) }
            }.build()
            override val params = emptyParams
            override val pageable = false
        }
        class Delete(channelId: String): Channel() {
            override val method = Method.POST
            override val bucket = "channel/delete"
            override val route = "channel/delete"
            override val postData = FormBody.Builder().add("channel_id", channelId).build()
            override val params = emptyParams
            override val pageable = false
        }
        class MoveUser(targetId: String, userIds: Array<String>): Channel() {
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
            }.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            override val params = emptyParams
            override val pageable = false
        }
    }
}

