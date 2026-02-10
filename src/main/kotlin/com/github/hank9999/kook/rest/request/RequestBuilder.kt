package com.github.hank9999.kook.rest.request

import com.github.hank9999.kook.rest.route.Route
import io.ktor.client.request.forms.ChannelProvider
import io.ktor.http.HeadersBuilder
import io.ktor.http.ParametersBuilder
import kotlinx.serialization.SerializationStrategy

/**
 * 构建 KOOK API 请求
 *
 * 用法:
 * ```kotlin
 * call(Route.MessageCreate) {
 *     body(MessageCreateRequest.serializer(), request)
 * }
 *
 * call(Route.UserView) {
 *     parameter("user_id", userId)
 * }
 * ```
 */
class RequestBuilder<T>(val route: Route<T>) {

    private val headers = HeadersBuilder()
    private val parameters = ParametersBuilder()
    private var body: RequestBody<*>? = null
    private val files: MutableList<NamedFile> = mutableListOf()

    fun <E : Any> body(strategy: SerializationStrategy<E>, body: E) {
        this.body = RequestBody(strategy, body)
    }

    fun parameter(key: String, value: Any) {
        parameters.append(key, value.toString())
    }

    fun header(key: String, value: String) {
        headers.append(key, value)
    }

    fun file(name: String, contentProvider: ChannelProvider) {
        files.add(NamedFile(name, contentProvider))
    }

    fun file(file: NamedFile) {
        files.add(file)
    }

    fun build(): Request<T> = when {
        files.isEmpty() -> JsonRequest(route, parameters.build(), headers.build(), body)
        else -> MultipartRequest(route, parameters.build(), headers.build(), body, files.toList())
    }
}
