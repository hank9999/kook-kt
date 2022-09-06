package com.github.hank9999.kook.types

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Role(
    @Required @SerialName("role_id") val roleId: Int = 0,
    @Required val name: String = "",
    val color: Int = 0,
    val position: Int = 0,
    val hoist: Int = 0,
    val mentionable: Int = 0,
    val permissions: Int = 0
)
