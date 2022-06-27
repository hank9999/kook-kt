package com.github.hank9999.khlKt.http

import com.github.hank9999.khlKt.http.types.Method
import okhttp3.FormBody

abstract class Api {
    abstract val method: Method
    abstract val bucket: String
    abstract val route: String
    abstract val formData: FormBody
    abstract val params: Map<String, String>
    abstract val paging: Boolean

    val emptyFormBody = FormBody.Builder().build()
    val emptyParams = emptyMap<String, String>()

    abstract class Guild: Api() {
        class List(sort: String? = null): Guild() {
            override val method = Method.GET
            override val bucket = "guild/list"
            override val route = "guild/list"
            override val formData = emptyFormBody
            override val params = if (sort != null) mapOf("sort" to sort) else emptyParams
            override val paging = true
        }
        class View(guildId: String): Guild() {
            override val method = Method.GET
            override val bucket = "guild/view"
            override val route = "guild/view"
            override val formData = emptyFormBody
            override val params = mapOf("guild_id" to guildId)
            override val paging = false
        }
        class UserList(guildId: String, channelId: String? = null, search: String? = null, roleId: Int? = null,
                       mobileVerified: Int? = null, activeTime: Int? = null, joinedAt: Int? = null): Guild() {
            override val method = Method.GET
            override val bucket = "guild/user-list"
            override val route = "guild/user-list"
            override val formData = emptyFormBody
            override val params = mutableMapOf<String, String>().apply {
                this["guild_id"] = guildId
                channelId?.let { this["channel_id"] = channelId }
                search?.let { this["search"] = search }
                roleId?.let { this["role_id"] = roleId.toString() }
                mobileVerified?.let { require(mobileVerified in 0..1); this["mobile_verified"] = mobileVerified.toString() }
                activeTime?.let { require(activeTime in 0..1); this["active_time"] = activeTime.toString() }
                joinedAt?.let { require(joinedAt in 0..1); this["joined_at"] = joinedAt.toString()}
            }
            override val paging = true
        }
        class Nickname(guildId: String, nickname: String? = null, userId: String? = null): Guild() {
            override val method = Method.POST
            override val bucket = "guild/nickname"
            override val route = "guild/nickname"
            override val formData = FormBody.Builder().apply {
                this.add("guild_id", guildId)
                this.add("nickname", nickname ?: "")
                userId?.let { this.add("user_id", userId) }
            }.build()
            override val params = emptyParams
            override val paging = false
        }
        class Leave(guildId: String): Guild() {
            override val method = Method.POST
            override val bucket = "guild/leave"
            override val route = "guild/leave"
            override val formData = FormBody.Builder().add("guild_id", guildId).build()
            override val params = emptyParams
            override val paging = false
        }
        class KickOut(guildId: String, targetId: String): Guild() {
            override val method = Method.POST
            override val bucket = "guild/nickname"
            override val route = "guild/nickname"
            override val formData = FormBody.Builder()
                .add("guild_id", guildId)
                .add("target_id", targetId)
                .build()
            override val params = emptyParams
            override val paging = false
        }
    }
    abstract class GuildMute: Api() {
        class List(guildId: String, returnType: String? = null): GuildMute() {
            override val method = Method.GET
            override val bucket = "guild-mute/list"
            override val route = "guild-mute/list"
            override val formData = emptyFormBody
            override val params = mutableMapOf<String, String>().apply {
                this["guild_id"] = guildId
                returnType?.let { this["return_type"] = returnType }
            }
            override val paging = false
        }
        class Create(guildId: String, userId: String, type: Int): GuildMute() {
            override val method = Method.POST
            override val bucket = "guild-mute/create"
            override val route = "guild-mute/create"
            override val formData = FormBody.Builder()
                .add("guild_id", guildId)
                .add("user_id", userId)
                .add("type", type.toString())
                .build()
            override val params = emptyParams
            override val paging = false
        }
        class Delete(guildId: String, userId: String, type: Int): GuildMute() {
            override val method = Method.POST
            override val bucket = "guild-mute/delete"
            override val route = "guild-mute/delete"
            override val formData = FormBody.Builder()
                .add("guild_id", guildId)
                .add("user_id", userId)
                .add("type", type.toString())
                .build()
            override val params = emptyParams
            override val paging = false
        }
    }
}

