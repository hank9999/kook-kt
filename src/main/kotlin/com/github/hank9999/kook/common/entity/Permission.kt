package com.github.hank9999.kook.common.entity

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = IPermissionOverwriteSerializer::class) // <--- 修改这里
sealed interface IPermissionOverwrite {
    val roleId: Int
    val allow: Int
    val deny: Int
}

@Serializable(with = IPermissionUserSerializer::class) // <--- 修改这里
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
    override val user: IUser,
    @SerialName("user_id") override val userId: String,
    override val allow: Int,
    override val deny: Int
) : IPermissionUser

internal object IPermissionOverwriteSerializer : KSerializer<IPermissionOverwrite> {
    override val descriptor: SerialDescriptor = PermissionOverwrite.serializer().descriptor
    override fun serialize(encoder: Encoder, value: IPermissionOverwrite) = encoder.encodeSerializableValue(PermissionOverwrite.serializer(), value as PermissionOverwrite)
    override fun deserialize(decoder: Decoder): IPermissionOverwrite = decoder.decodeSerializableValue(PermissionOverwrite.serializer())
}

internal object IPermissionUserSerializer : KSerializer<IPermissionUser> {
    override val descriptor: SerialDescriptor = PermissionUser.serializer().descriptor
    override fun serialize(encoder: Encoder, value: IPermissionUser) = encoder.encodeSerializableValue(PermissionUser.serializer(), value as PermissionUser)
    override fun deserialize(decoder: Decoder): IPermissionUser = decoder.decodeSerializableValue(PermissionUser.serializer())
}