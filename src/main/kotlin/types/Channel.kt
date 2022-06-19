package types

import types.types.ChannelTypes
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    @Required val id: String = "",
    @Required val name: String = "",
    @Required val user_id: String = "",
    @Required val guild_id: String = "",
    @Required val topic: String = "",
    @Required val parent_id: String = "",
    @Required val level: Int = 0,
    @Required val slow_mode: Int = 0,
    @Required val type: ChannelTypes = ChannelTypes.TEXT,
    @Required val permission_overwrites: PermissionOverwrites = PermissionOverwrites(),
    @Required val permission_users: PermissionUsers = PermissionUsers(),
    @Required val permission_sync: Int = 0,
    val has_password: Boolean = false
) {
    @Serializable
    data class PermissionOverwrites(
        @Required val role_id: Int = 0,
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