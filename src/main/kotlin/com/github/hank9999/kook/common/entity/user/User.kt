package com.github.hank9999.kook.common.entity.user

import com.github.hank9999.kook.common.interfaces.IUser
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class User(
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
    @SerialName("is_sys") override val isSys: Boolean
) : IUser

