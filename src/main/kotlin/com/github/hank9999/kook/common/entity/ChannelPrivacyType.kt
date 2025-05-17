package com.github.hank9999.kook.common.entity

import kotlinx.serialization.Serializable

@Serializable
enum class ChannelPrivacyType {
    GROUP,
    PERSON,
    BROADCAST,
    UNKNOWN;

    companion object {
        fun fromValue(value: String): ChannelPrivacyType = when (value.uppercase()) {
            "GROUP" -> GROUP
            "PERSON" -> PERSON
            "BROADCAST" -> BROADCAST
            else -> UNKNOWN
        }
    }
}