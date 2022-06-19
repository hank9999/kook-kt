package types.types

import kotlinx.serialization.Serializable

@Serializable
enum class ChannelPrivacyTypes {
    GROUP,
    PERSON
}