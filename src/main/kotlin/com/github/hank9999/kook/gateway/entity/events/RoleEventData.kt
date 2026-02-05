package com.github.hank9999.kook.gateway.entity.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 角色事件数据
 * 用于 added_role、deleted_role、updated_role 事件
 */
@Serializable
data class RoleEventData(
    @SerialName("role_id") val roleId: Int,
    val name: String,
    val color: Int = 0,
    val position: Int = 0,
    val hoist: Int = 0,
    val mentionable: Int = 0,
    val permissions: Int = 0,
)
