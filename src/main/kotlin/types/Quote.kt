package types

import types.kmd.KMarkdown
import types.types.MessageTypes
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class Quote(
    @Required val id: String = "",
    @Required val rong_id: String = "",
    @Required val type: MessageTypes = MessageTypes.KMD,
    @Required val content: String = "",
    @Required val create_at: Long = 0,
    @Required val author: User = User(),
    val kmarkdown: KMarkdown = KMarkdown()
)