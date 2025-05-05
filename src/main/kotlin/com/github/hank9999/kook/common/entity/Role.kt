package com.github.hank9999.kook.common.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Role(
    @SerialName("role_id") val roleId: Int,
    val name: String,
    val desc: String,
    val color: Int,
    @SerialName("color_type") val colorType: Int,
    @SerialName("color_map") val colorMap: ColorMap,
    val position: Int,
    val hoist: Int,
    val mentionable: Int,
    val permissions: Int,
    val type: Int,
    @SerialName("op_permissions") val opPermissions: Int
) {
    @Serializable
    data class ColorMap(
        @SerialName("color_list") val colorList: List<Int>
    )
}
