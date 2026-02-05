package com.github.hank9999.kook.common.entity

import kotlinx.serialization.Serializable

/**
 * 表情实体类
 * 用于表情反应事件中的 emoji 字段
 */
@Serializable
data class Emoji(
    val id: String,
    val name: String,
)
