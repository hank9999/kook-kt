package com.github.hank9999.kook.common.interfaces

interface IChannel {
    val id: String
    val guildId: String
    val masterId: String
    val parentId: String
    val userId: String
    val name: String
    val topic: String
    val type: Int
    val level: Int
    val slowMode: Int
    val lastMsgContent: String
    val lastMsgId: String
    val hasPassword: Boolean
    val limitAmount: Int
    val isCategory: Boolean
    val permissionSync: Int
    val permissionOverwrites: List<IPermissionOverwrite>
    val permissionUsers: List<IPermissionUser>

    interface IPermissionOverwrite {
        val roleId: Int
        val allow: Int
        val deny: Int
    }

    interface IPermissionUser {
        val user: IUser
        val userId: String
        val allow: Int
        val deny: Int
    }
}