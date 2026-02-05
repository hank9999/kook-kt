package com.github.hank9999.kook.common.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class KMarkdown(
    @SerialName("raw_content") val rawContent: String = "",
    @SerialName("mention_part") val mentionPart: List<MentionPart> = emptyList(),
    @SerialName("mention_role_part") val mentionRolePart: List<MentionRolePart> = emptyList(),
) {
    @Serializable
    data class MentionPart(
        val id: String,
        val username: String,
        @SerialName("full_name") val fullName: String = "",
        val avatar: String = "",
    )

    @Serializable
    data class MentionRolePart(
        @SerialName("role_id") val roleId: Int,
        val name: String,
        val color: Int = 0,
    )
}
