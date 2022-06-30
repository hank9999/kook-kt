package com.github.hank9999.kook.types.kmd

import com.github.hank9999.kook.types.Role
import kotlinx.serialization.Serializable

@Serializable
data class KMarkdown(
    val raw_content: String = "",
    val mention_part: List<MentionPart> = emptyList(),
    val mention_role_part: List<Role> = emptyList()
)