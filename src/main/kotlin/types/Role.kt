package types

import kotlinx.serialization.Required
import kotlinx.serialization.Serializable

@Serializable
data class Role(
    @Required val role_id: Int = 0,
    @Required val name: String = "",
    val color: Int = 0,
    val position: Int = 0,
    val hoist: Int = 0,
    val mentionable: Int = 0,
    val permissions: Int = 0
)
