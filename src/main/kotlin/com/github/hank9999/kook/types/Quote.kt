package com.github.hank9999.kook.types

import com.github.hank9999.kook.types.kmd.KMarkdown
import com.github.hank9999.kook.types.types.MessageTypes
import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Quote(
    @Required val id: String = "",
    @Required @SerialName("rong_id") val rongId: String = "",
    @Required val type: MessageTypes = MessageTypes.KMD,
    @Required val content: String = "",
    @Required @SerialName("create_at") val createAt: Long = 0,
    @Required val author: User = User(),
    val kmarkdown: KMarkdown = KMarkdown()
)