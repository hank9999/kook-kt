package com.github.hank9999.khlKt.types.types

import kotlinx.serialization.Serializable

@Serializable
enum class ChannelPrivacyTypes {
    GROUP,
    PERSON,
    BROADCAST
}