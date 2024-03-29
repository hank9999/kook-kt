package com.github.hank9999.kook.types.kmd

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MentionPart(
    val id: String = "",
    val username: String = "",
    @SerialName("full_name") val fullName: String = "",
    val avatar: String = ""
)