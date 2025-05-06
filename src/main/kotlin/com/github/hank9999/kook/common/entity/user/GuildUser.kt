package com.github.hank9999.kook.common.entity.user

import com.github.hank9999.kook.common.entity.Optional
import com.github.hank9999.kook.common.interfaces.IUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GuildUser(
    override val id: String,
    override val username: String,
    override val nickname: String,
    @SerialName("identify_num") override val identifyNum: String,
    override val online: Boolean,
    override val os: String,
    override val status: Int,
    override val avatar: String,
    @SerialName("vip_avatar") override val vipAvatar: String,
    override val banner: String,
    @SerialName("is_vip") override val isVip: Boolean,
    @SerialName("vip_amp") override val vipAmp: Boolean,
    override val bot: Boolean,
    override val roles: List<Int>,
    @SerialName("is_sys") override val isSys: Boolean,

    // --- Guild 特有字段 ---
    @SerialName("mobile_verified") val mobileVerified: Boolean,
    @SerialName("joined_at") val joinedAt: Long,
    @SerialName("active_time") val activeTime: Long,

    // --- Bot 特有字段 (设为可空) ---
    @SerialName("bot_status") val botStatus: Optional<Int> = Optional.Missing(),
    @SerialName("tag_info") val tagInfo: Optional<TagInfo> = Optional.Missing(),
    @SerialName("client_id") val clientId: Optional<String> = Optional.Missing(),
    val verified: Optional<Boolean> = Optional.Missing(),
) : IUser

