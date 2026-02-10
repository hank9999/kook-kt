package com.github.hank9999.kook.rest.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * KOOK API 统一响应包装
 *
 * 所有 API 响应格式:
 * ```json
 * { "code": 0, "message": "操作成功", "data": ... }
 * ```
 */
@Serializable
data class KookApiResponse(
    val code: Int,
    val message: String,
    val data: JsonElement,
)

/**
 * KOOK 分页数据结构
 *
 * 分页接口的 data 格式:
 * ```json
 * { "items": [...], "meta": {...}, "sort": {...} }
 * ```
 */
@Serializable
data class PagedData<T>(
    val items: List<T>,
    val meta: PageMeta,
    val sort: Map<String, Int> = emptyMap(),
)

/**
 * 分页元信息
 */
@Serializable
data class PageMeta(
    val page: Int,
    @SerialName("page_total")
    val pageTotal: Int,
    @SerialName("page_size")
    val pageSize: Int,
    val total: Int,
)
