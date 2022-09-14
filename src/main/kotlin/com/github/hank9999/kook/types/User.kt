package com.github.hank9999.kook.types

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
    @Required val id: String = "",
    @Required val username: String = "",
    @Required val nickname: String = "",
    @Required @SerialName("identify_num") val identifyNum: String = "",
    @Required val online: Boolean = false,
    @Required val bot: Boolean = false,
    @Required val status: Int = 0,
    @Required val avatar: String = "",
    @Required @SerialName("vip_avatar") val vipAvatar: String = "",
    @Required @SerialName("is_vip") val isVip: Boolean = false,
    @Required val banner: String = "",
    @SerialName("mobile_verified") val mobileVerified: Boolean = true,
    @Required val roles: List<Int> = emptyList(),
    @SerialName("joined_at") val joinedAt: Long = 0,
    @SerialName("active_time") val activeTime: Long = 0,
    val os: String = "",
    @SerialName("live_info") val liveInfo: LiveInfo = LiveInfo()
) {
    @Serializable
    data class LiveInfo(
        @SerialName("audience_count") val audienceCount: Int = 0,
        @SerialName("in_live") val inLive: Boolean = false,
        @SerialName("live_start_time") val liveStartTime: Long = 0,
        @SerialName("live_thumb") val liveThumb: String = ""
    )
}

