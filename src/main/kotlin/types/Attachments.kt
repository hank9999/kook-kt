package types

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class Attachments(
    @Required val type: String = "",
    @Required val url: String = "",
    @Required val name: String = "",
    val file_type: String = "",
    val size: Int = 0,
    val duration: Int = 0,
    val width: Int = 0,
    val height: Int = 0
)
