package com.github.hank9999.kook.rest.route

import io.ktor.http.HttpMethod
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * 响应 data 字段的反序列化映射器
 */
sealed interface ResponseMapper<T> {
    fun deserialize(json: Json, element: JsonElement): T
}

internal class ValueMapper<T>(
    val strategy: DeserializationStrategy<T>,
) : ResponseMapper<T> {
    override fun deserialize(json: Json, element: JsonElement): T =
        json.decodeFromJsonElement(strategy, element)

    override fun toString(): String = "ValueMapper(strategy=$strategy)"
}

internal object UnitMapper : ResponseMapper<Unit> {
    override fun deserialize(json: Json, element: JsonElement) = Unit
    override fun toString(): String = "UnitMapper"
}

/**
 * KOOK API 路由定义
 *
 * - 路径无占位符 (KOOK 的 ID 在 query/body 中传递)
 * - 路径是纯字符串, 不含变量替换
 *
 * @param method HTTP 方法 (GET 或 POST)
 * @param path 相对路径, 如 "message/create" (不含 /api/v3/ 前缀)
 * @param mapper 响应 data 字段的反序列化策略
 */
sealed class Route<T>(
    val method: HttpMethod,
    val path: String,
    val mapper: ResponseMapper<T>,
) {
    companion object {
        const val BASE_URL: String = "https://www.kookapp.cn/api/v3/"
    }

    protected constructor(
        method: HttpMethod,
        path: String,
        strategy: DeserializationStrategy<T>,
    ) : this(method, path, ValueMapper(strategy))

    override fun toString(): String = "Route(${method.value} $path)"
}
