package com.github.hank9999.kook.types.kmd.itempart

import kotlinx.serialization.Serializable

@Serializable
data class QualityResource(
    val big: String = "",
    val color: String = "",
    val small: String = ""
)