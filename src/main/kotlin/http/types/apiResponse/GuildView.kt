package http.types.apiResponse

import types.Channel
import types.Role
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class GuildView(
    @Required val id: String = "",
    @Required val name: String = "",
    @Required val topic: String = "",
    @Required val master_id: String = "",
    @Required val icon: String = "",
    @Required val notify_type: Int = 0,
    @Required val region: String = "",
    @Required val enable_open: Boolean = false,
    @Required val open_id: String = "",
    @Required val default_channel_id: String = "",
    @Required val welcome_channel_id: String = "",
    @Required val roles: List<Role> = emptyList(),
    @Required val channels: Channel = Channel()
)