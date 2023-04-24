package com.github.hank9999.kook.types.kmd

import com.github.hank9999.kook.types.Role
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MentionInfo(
    @SerialName("mention_part") val mentionPart: List<MentionPart> = emptyList(),
    @SerialName("mention_role_part") val mentionRolePart: List<Role> = emptyList(),
    @SerialName("item_part") val itemPart: List<ItemPart> = emptyList()
)
