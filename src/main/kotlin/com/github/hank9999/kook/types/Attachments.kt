package com.github.hank9999.kook.types

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attachments(
    @Required val type: String = "",
    @Required val url: String = "",
    @Required val name: String = "",
    @SerialName("file_type") val fileType: String = "",
    val size: Int = 0,
    val duration: Double = 0.0,
    val width: Int = 0,
    val height: Int = 0
)
