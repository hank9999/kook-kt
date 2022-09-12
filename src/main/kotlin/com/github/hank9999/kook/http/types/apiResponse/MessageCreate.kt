package com.github.hank9999.kook.http.types.apiResponse

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageCreate(
    @Required @SerialName("msg_id") val msgId: String = "",
    @Required @SerialName("msg_timestamp") val msgTimestamp: Long = 0,
    val nonce: String = ""
)
