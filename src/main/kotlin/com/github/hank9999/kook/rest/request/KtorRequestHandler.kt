package com.github.hank9999.kook.rest.request

import com.github.hank9999.kook.common.utils.JSON
import com.github.hank9999.kook.rest.exception.KookRestRequestException
import com.github.hank9999.kook.rest.ratelimit.ExclusionRequestRateLimiter
import com.github.hank9999.kook.rest.ratelimit.RequestRateLimiter
import com.github.hank9999.kook.rest.ratelimit.RequestResponse
import com.github.hank9999.kook.rest.ratelimit.consume
import com.github.hank9999.kook.rest.response.KookApiResponse
import com.github.hank9999.kook.rest.route.Route
import com.github.hank9999.kook.rest.route.UnitMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.content.TextContent
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import java.io.Closeable
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds

/**
 * 基于 Ktor 的 [RequestHandler] 实现
 *
 * 核心流程:
 * 1. rate limiter 等待许可
 * 2. 执行 HTTP 请求
 * 3. 从响应头解析 rate limit 信息
 * 4. 429 → 循环重试 (最多 [maxRetries] 次)
 * 5. 502/503/504 → 指数退避重试 (最多 [maxTransientRetries] 次)
 * 6. 拆包 KookApiResponse → 检查 code → 反序列化 data
 */
class KtorRequestHandler(
    private val client: HttpClient,
    private val requestRateLimiter: RequestRateLimiter = ExclusionRequestRateLimiter(),
    private val json: Json = JSON.defaultJson(),
    override val token: String,
    private val maxRetries: Int = 5,
    private val maxTransientRetries: Int = 3,
) : RequestHandler, Closeable {

    private val logger = KotlinLogging.logger {}

    override suspend fun <T> handle(request: Request<T>): T {
        var retries = 0
        var transientRetries = 0
        while (true) {
            val response = requestRateLimiter.consume(request) { requestToken ->
                val httpResponse = executeRequest(request)
                requestToken.complete(RequestResponse.from(httpResponse))
                httpResponse
            }

            val body = response.bodyAsText()

            when {
                response.isRateLimit -> {
                    retries++
                    if (retries > maxRetries) {
                        throw KookRestRequestException(
                            request = request,
                            status = 429,
                            kookCode = null,
                            kookMessage = "Rate limit exceeded after $maxRetries retries for ${request.route}",
                        )
                    }
                    // 服务端未返回 X-Rate-Limit-Reset 时, 兜底延迟防止紧密循环
                    if (response.rateLimitResetSeconds == null) {
                        delay(1.seconds)
                    }
                    logger.debug { "[RATE_LIMIT] ${request.route} -> 429, retrying ($retries/$maxRetries)..." }
                }
                response.isTransientError -> {
                    transientRetries++
                    if (transientRetries > maxTransientRetries) {
                        throw KookRestRequestException(
                            request = request,
                            status = response.status.value,
                            kookCode = null,
                            kookMessage = "Server error ${response.status.value} after $maxTransientRetries retries for ${request.route}",
                        )
                    }
                    // 指数退避: 1s, 2s, 4s
                    val backoff = (1.seconds * (1 shl (transientRetries - 1)))
                    logger.debug { "[TRANSIENT] ${request.route} -> ${response.status.value}, retrying in $backoff ($transientRetries/$maxTransientRetries)..." }
                    delay(backoff)
                }
                response.isError -> {
                    logger.debug { "[ERROR] ${request.route} -> ${response.status.value}: $body" }
                    throw KookRestRequestException(
                        request = request,
                        status = response.status.value,
                        kookCode = null,
                        kookMessage = body,
                    )
                }
                else -> {
                    logger.debug { "[OK] ${request.route} -> ${response.status.value}" }
                    return unwrapResponse(request, body)
                }
            }
        }
    }

    /**
     * 拆包 KOOK 统一响应 `{code, message, data}` 并反序列化 data 字段
     */
    private fun <T> unwrapResponse(request: Request<T>, body: String): T {
        val apiResponse = json.decodeFromString(KookApiResponse.serializer(), body)

        if (apiResponse.code != 0) {
            throw KookRestRequestException(
                request = request,
                status = 200,
                kookCode = apiResponse.code,
                kookMessage = apiResponse.message,
            )
        }

        val mapper = request.route.mapper
        if (mapper is UnitMapper) {
            @Suppress("UNCHECKED_CAST")
            return Unit as T
        }

        val data = apiResponse.data
        if (data is JsonNull) {
            throw KookRestRequestException(
                request = request,
                status = 200,
                kookCode = apiResponse.code,
                kookMessage = "Expected non-null data for ${request.route}, but got null",
            )
        }
        return mapper.deserialize(json, data)
    }

    private suspend fun <T> executeRequest(request: Request<T>): HttpResponse {
        return client.request(Route.BASE_URL + request.route.path) {
            method = request.route.method

            request.headers.forEach { name, values ->
                values.forEach { value -> header(name, value) }
            }
            request.parameters.forEach { name, values ->
                values.forEach { value -> parameter(name, value) }
            }

            when (request) {
                is JsonRequest -> {
                    val requestBody = request.body ?: return@request
                    val jsonStr = requestBody.encode(json)
                    logger.debug { request.logString(jsonStr) }
                    setBody(TextContent(jsonStr, ContentType.Application.Json))
                }
                is MultipartRequest -> {
                    val parts = formData {
                        request.body?.let {
                            append(
                                "payload_json",
                                it.encode(json),
                                Headers.build {
                                    append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                                },
                            )
                        }
                        request.files.forEach { (name, provider) ->
                            append(name, provider, Headers.build {
                                append(HttpHeaders.ContentDisposition, "filename=$name")
                            })
                        }
                    }
                    setBody(MultiPartFormDataContent(parts))
                }
            }
        }
    }

    override fun close() {
        client.close()
    }

    companion object {
        fun create(
            token: String,
            requestRateLimiter: RequestRateLimiter = ExclusionRequestRateLimiter(),
            json: Json = JSON.defaultJson(),
        ): KtorRequestHandler {
            val client = HttpClient(OkHttp) {
                expectSuccess = false
                engine {
                    config {
                        connectTimeout(10, TimeUnit.SECONDS)
                        readTimeout(30, TimeUnit.SECONDS)
                        writeTimeout(30, TimeUnit.SECONDS)
                        callTimeout(3, TimeUnit.MINUTES)
                    }
                }
            }
            return KtorRequestHandler(client, requestRateLimiter, json, token)
        }
    }
}
