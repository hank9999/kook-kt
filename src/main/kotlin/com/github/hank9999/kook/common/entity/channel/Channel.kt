package com.github.hank9999.kook.common.entity.channel

import com.github.hank9999.kook.common.entity.user.GuildUser
import com.github.hank9999.kook.common.interfaces.IChannel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
    @SerialName("permission_overwrites") override val permissionOverwrites: List<PermissionOverwrite>,
    @SerialName("permission_users") override val permissionUsers: List<PermissionUser>
) : IChannel {
    @Serializable
    data class PermissionOverwrite(
        @SerialName("role_id") override val roleId: Int,
        override val allow: Int,
        override val deny: Int
    ) : IChannel.IPermissionOverwrite
    @Serializable
    data class PermissionUser(
        override val user: GuildUser,
        override val allow: Int,
        override val deny: Int
    ) : IChannel.IPermissionUser
}
