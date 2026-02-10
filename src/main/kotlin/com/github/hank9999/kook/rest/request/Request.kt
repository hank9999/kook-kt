package com.github.hank9999.kook.rest.request

import com.github.hank9999.kook.rest.route.Route
import io.ktor.client.request.forms.ChannelProvider
import io.ktor.http.Headers
import io.ktor.http.Parameters
import kotlinx.serialization.SerializationStrategy

/**
 * 序列化请求体
 */
data class RequestBody<T : Any>(
    val strategy: SerializationStrategy<T>,
    val body: T,
)

/**
 * 命名文件 (用于 multipart 上传)
 */
data class NamedFile(
    val name: String,
    val contentProvider: ChannelProvider,
)

/**
 * KOOK API 请求数据模型
 *
 * - 单泛型 T 表示响应类型
 * - 无 routeParams (KOOK 路径无占位符)
 * - baseUrl 固定为 [Route.BASE_URL]
 */
sealed class Request<T> {
    abstract val route: Route<T>
    abstract val headers: Headers
    abstract val parameters: Parameters
    abstract val body: RequestBody<*>?
    abstract val files: List<NamedFile>
}

class JsonRequest<T>(
    override val route: Route<T>,
    override val parameters: Parameters,
    override val headers: Headers,
    override val body: RequestBody<*>?,
) : Request<T>() {
    override val files: List<NamedFile> = emptyList()
}

class MultipartRequest<T>(
    override val route: Route<T>,
    override val parameters: Parameters,
    override val headers: Headers,
    override val body: RequestBody<*>?,
    override val files: List<NamedFile>,
) : Request<T>()
