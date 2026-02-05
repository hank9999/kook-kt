package com.github.hank9999.kook.common.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Attachment(
    val type: String = "",
    val url: String = "",
    val name: String = "",
    val size: Int? = null,
    @SerialName("file_type") val fileType: String? = null,
    val duration: Double? = null,
    val width: Int? = null,
    val height: Int? = null,
)
