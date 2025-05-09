package com.github.hank9999.kook.common.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface IChannel {
    val id: String
    val guildId: String
    val masterId: String
    val parentId: String
    val userId: String
    val name: String
    val topic: String
    val type: Int
    val level: Int
    val slowMode: Int
    val lastMsgContent: String
    val lastMsgId: String
    val hasPassword: Boolean
    val limitAmount: Int
    val isCategory: Boolean
    val permissionSync: Int
    val permissionOverwrites: List<IPermissionOverwrite>
    val permissionUsers: List<IPermissionUser>
}

@Serializable
data class Channel(
    override val id: String,
    @SerialName("guild_id") override val guildId: String,
    @SerialName("master_id") override val masterId: String,
    @SerialName("parent_id") override val parentId: String,
    @SerialName("user_id") override val userId: String,
    override val name: String,
    override val topic: String,
    override val type: Int,
    override val level: Int,
    @SerialName("slow_mode") override val slowMode: Int,
    @SerialName("last_msg_content") override val lastMsgContent: String,
    @SerialName("last_msg_id") override val lastMsgId: String,
    @SerialName("has_password") override val hasPassword: Boolean,
    @SerialName("limit_amount") override val limitAmount: Int,
    @SerialName("is_category") override val isCategory: Boolean,
    @SerialName("permission_sync") override val permissionSync: Int,
    @SerialName("permission_overwrites") override val permissionOverwrites: List<IPermissionOverwrite>,
    @SerialName("permission_users") override val permissionUsers: List<IPermissionUser>
) : IChannel

@Serializable
data class DetailChannel(
    override val id: String,
    @SerialName("guild_id") override val guildId: String,
    @SerialName("master_id") override val masterId: String,
    @SerialName("parent_id") override val parentId: String,
    @SerialName("user_id") override val userId: String,
    override val name: String,
    override val topic: String,
    override val type: Int,
    override val level: Int,
    @SerialName("slow_mode") override val slowMode: Int,
    @SerialName("last_msg_content") override val lastMsgContent: String,
    @SerialName("last_msg_id") override val lastMsgId: String,
    @SerialName("has_password") override val hasPassword: Boolean,
    @SerialName("limit_amount") override val limitAmount: Int,
    @SerialName("is_category") override val isCategory: Boolean,
    @SerialName("permission_sync") override val permissionSync: Int,
    @SerialName("permission_overwrites") override val permissionOverwrites: List<IPermissionOverwrite>,
    @SerialName("permission_users") override val permissionUsers: List<IPermissionUser>,

    // --- channel/view 特有字段 ---
    @SerialName("voice_quality") val voiceQuality: String,
    @SerialName("server_url") val serverUrl: String,
    // val children: List<Channel>,
    val region: String,
    @SerialName("sync_guild_region") val syncGuildRegion: Int,
) : IChannel