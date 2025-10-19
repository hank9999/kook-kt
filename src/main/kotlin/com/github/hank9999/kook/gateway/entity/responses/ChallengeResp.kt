package com.github.hank9999.kook.gateway.entity.responses

import kotlinx.serialization.Serializable

@Serializable
data class ChallengeResp(
    val challenge: String
)
