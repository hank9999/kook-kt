package com.github.hank9999.kook.common.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TagInfo(
    val color: String,
    @SerialName("bg_color") val bgColor: String,
    val text: String
)