package com.github.hank9999.kook.common.entity.user

import com.github.hank9999.kook.common.entity.user.TagInfo
import com.github.hank9999.kook.common.interfaces.IUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SelfUser(
    val user: User,
    @SerialName("bot_status") val botStatus: Int,
    val tagInfo: TagInfo,
    @SerialName("mobile_verified") val mobileVerified: Boolean,
    @SerialName("client_id") val clientId: String,
    val verified: Boolean,
    @SerialName("mobile_prefix") val mobilePrefix: String,
    val mobile: String,
    @SerialName("invited_count") val invitedCount: Int
) : IUser by user