package com.github.hank9999.kook.types.kmd

import com.github.hank9999.kook.types.kmd.itempart.MsgScenarios
import com.github.hank9999.kook.types.kmd.itempart.QualityResource
import com.github.hank9999.kook.types.kmd.itempart.Resources
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemPart(
    val id: Int = 0,
    val name: String = "",
    val desc: String = "",
    val cd: Int = 0,
    val categories: List<String> = listOf(),
    val label: Int = 0,
    @SerialName("label_name") val labelName: String = "",
    val icon: String = "",
    @SerialName("icon_expired") val iconExpired: String = "",
    val quality: Int = 0,
    @SerialName("quality_resource") val qualityResource: QualityResource = QualityResource(),
    val resources: Resources = Resources(),
    @SerialName("msg_scenarios") val msgScenarios: MsgScenarios = MsgScenarios()
)