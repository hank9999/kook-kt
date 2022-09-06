package com.github.hank9999.kook.types.kmd.itempart

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Resources(
    val gif: String = "",
    val height: Int = 0,
    val pag: String = "",
    val percent: Int = 0,
    @SerialName("preview_expired") val previewExpired: String = "",
    val time: Int = 0,
    val type: String = "",
    val webp: String = "",
    val width: Int = 0
)