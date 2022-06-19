package com.github.hank9999.khlKt.types

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @Required val id: String = "",
    @Required val username: String = "",
    @Required val nickname: String = "",
    @Required val identify_num: String = "",
    @Required val online: Boolean = false,
    @Required val bot: Boolean = false,
    @Required val status: Int = 0,
    @Required val avatar: String = "",
    @Required val vip_avatar: String = "",
    @Required val is_vip: Boolean = false,
    val banner: String = "",
    val mobile_verified: Boolean = true,
    @Required val roles: List<Int> = emptyList(),
    val joined_at: Long = 0,
    val active_time: Long = 0
)
