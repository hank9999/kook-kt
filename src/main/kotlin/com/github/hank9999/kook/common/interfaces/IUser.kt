package com.github.hank9999.kook.common.interfaces


interface IUser {
    val id: String
    val username: String
    val nickname: String
    val identifyNum: String
    val online: Boolean
    val os: String
    val status: Int
    val avatar: String
    val vipAvatar: String
    val banner: String
    val isVip: Boolean
    val vipAmp: Boolean
    val bot: Boolean
    val roles: List<Int>
    val isSys: Boolean
}