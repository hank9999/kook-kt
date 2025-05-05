package com.github.hank9999.kook.common.entity.user

import com.github.hank9999.kook.common.entity.Optional
import com.github.hank9999.kook.common.entity.user.TagInfo
import com.github.hank9999.kook.common.interfaces.IUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GuildUser(
    val user: User,
    @SerialName("mobile_verified") val mobileVerified: Boolean,
    @SerialName("joined_at") val joinedAt: Long,
    @SerialName("active_time") val activeTime: Long,

    // --- Bot 特有字段 (设为可空) ---
    @SerialName("bot_status") val botStatus: Optional<Int> = Optional.Missing(),
    @SerialName("tag_info") val tagInfo: Optional<TagInfo> = Optional.Missing(),
    @SerialName("client_id") val clientId: Optional<String> = Optional.Missing(),
    val verified: Optional<Boolean> = Optional.Missing(),
) : IUser by user

