package com.github.hank9999.kook.gateway.entity.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * 角色事件数据
 * 用于 added_role、deleted_role、updated_role 事件
 */
@Serializable
data class RoleEventData(
    @SerialName("role_id") val roleId: Int,
    val name: String,
    val color: Int = 0,
    @SerialName("color_type") val colorType: Int = 0,
    @SerialName("color_map") val colorMap: JsonElement? = null,
    val position: Int = 0,
    val hoist: Int = 0,
    val mentionable: Int = 0,
    val permissions: Long = 0,
    val type: Int = 0,
    @SerialName("op_permissions") val opPermissions: Long = 0,
)
