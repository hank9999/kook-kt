package com.github.hank9999.kook.common.interfaces

import kotlinx.serialization.SerialName

interface IGuildUser : IUser {
    // --- Guild 特有字段 ---
    val mobileVerified: Boolean
    val joinedAt: Long
    val activeTime: Long
    // nameplate 未知用途 List类型 内部类型未知 仅在permission_users的user中

    // decorations_id_map kv类型 未知用途 仅在permission_users的user中, 下为该字段中已知的字段
    // "join_voice": Int,
    // "avatar_border": Int,
    // "background": Int,
    // "nameplate": Int,
    // "nameplates": List<Int>
}