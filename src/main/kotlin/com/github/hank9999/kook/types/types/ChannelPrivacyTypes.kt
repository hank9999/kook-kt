package com.github.hank9999.kook.types.types

import kotlinx.serialization.Serializable

@Serializable
enum class ChannelPrivacyTypes {
    GROUP,
    PERSON,
    BROADCAST
}