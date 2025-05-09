package com.github.hank9999.kook.common.entity

import com.github.hank9999.kook.common.serializers.UserSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface IPermissionOverwrite {
    val roleId: Int
    val allow: Int
    val deny: Int
}

sealed interface IPermissionUser {
    val user: IUser
    val userId: String
    val allow: Int
    val deny: Int
}

@Serializable
data class PermissionOverwrite(
    @SerialName("role_id") override val roleId: Int,
    override val allow: Int,
    override val deny: Int
) : IPermissionOverwrite

@Serializable
data class PermissionUser(
    @Serializable(with = UserSerializer::class) override val user: IUser,
    @SerialName("user_id") override val userId: String,
    override val allow: Int,
    override val deny: Int
) : IPermissionUser