package com.github.hank9999.kook.types

import com.github.hank9999.kook.types.types.ChannelTypes
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    @Required val id: String = "",
    @Required val name: String = "",
    @SerialName("master_id") val masterId: String = "",
    @Required @SerialName("user_id") val userId: String = "",
    @Required @SerialName("guild_id") val guildId: String = "",
    @Required val topic: String = "",
    @Required @SerialName("parent_id") val parentId: String = "",
    @Required val level: Int = 0,
    @Required @SerialName("slow_mode") val slowMode: Int = 0,
    @Required val type: ChannelTypes = ChannelTypes.TEXT,
    @SerialName("limit_amount") val limitAmount: Int = 0,
    @Required @SerialName("permission_overwrites") val permissionOverwrites: List<PermissionOverwrites> = emptyList(),
    @Required @SerialName("permission_users") val permissionUsers: List<PermissionUsers> = emptyList(),
    @Required @SerialName("permission_sync") val permissionSync: Int = 0,
    @SerialName("has_password") val has_password: Boolean = false
) {
    @Serializable
    data class PermissionOverwrites(
        @Required @SerialName("role_id") val roleId: Int = 0,
        @Required val allow: Int = 0,
        @Required val deny: Int = 0
    )
    @Serializable
    data class PermissionUsers(
        @Required val user: User = User(),
        @Required val allow: Int = 0,
        @Required val deny: Int = 0
    )
}