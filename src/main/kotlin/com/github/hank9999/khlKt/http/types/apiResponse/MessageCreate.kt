package com.github.hank9999.khlKt.http.types.apiResponse

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class MessageCreate(
    @Required val msg_id: String = "",
    @Required val msg_timestamp: Long = 0,
    val nonce: String = ""
)
