package com.github.hank9999.kook.http.types.apiResponse.guild

import com.github.hank9999.kook.types.Channel
import com.github.hank9999.kook.types.Role
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class View(
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
    @Required val channels: List<Channel> = emptyList(),
)