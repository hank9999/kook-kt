package com.github.hank9999.kook.types

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Emoji(
    @SerialName("id") val id: String = "",
    @SerialName("name") val name: String = ""
)