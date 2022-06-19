package types.kmd

import kotlinx.serialization.Serializable

@Serializable
data class MentionPart(
    val id: String = "",
    val username: String = "",
    val full_name: String = "",
    val avatar: String = ""
)