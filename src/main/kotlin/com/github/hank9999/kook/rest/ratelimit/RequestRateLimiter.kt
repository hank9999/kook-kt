package com.github.hank9999.kook.rest.ratelimit

import com.github.hank9999.kook.rest.request.Request
import com.github.hank9999.kook.rest.request.isError
import com.github.hank9999.kook.rest.request.isGlobalRateLimit
import com.github.hank9999.kook.rest.request.isRateLimit
import com.github.hank9999.kook.rest.request.rateLimitBucket
import com.github.hank9999.kook.rest.request.rateLimitLimit
import com.github.hank9999.kook.rest.request.rateLimitRemaining
import com.github.hank9999.kook.rest.request.rateLimitResetSeconds
import io.ktor.client.statement.HttpResponse
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

/**
 * KOOK API Rate Limiter 接口
 *
 * 适配 KOOK 的限速机制:
 * - Reset 是等待秒数而非 Unix 时间戳
 * - Bucket 标识从响应头 X-Rate-Limit-Bucket 获取
 */
interface RequestRateLimiter {
    /**
     * 等待 rate limit 许可, 返回 [RequestToken] 用于通知请求结果
     */
    suspend fun await(request: Request<*>): RequestToken
}

/**
 * 等待 rate limit 许可后执行 [consumer], 确保 token 被正确 complete
 */
@OptIn(ExperimentalContracts::class)
suspend inline fun <T> RequestRateLimiter.consume(
    request: Request<*>,
    consumer: (token: RequestToken) -> T,
): T {
    contract { callsInPlace(consumer, InvocationKind.EXACTLY_ONCE) }
    val token = await(request)
    try {
        val result = consumer(token)
        if (!token.completed) {
            token.complete(RequestResponse.Error)
            error("RequestToken was not completed")
        }
        return result
    } catch (e: Throwable) {
        if (!token.completed) token.complete(RequestResponse.Error)
        throw e
    }
}

/**
 * 请求令牌, 用于异步通知 rate limit 状态
 */
interface RequestToken {
    val completed: Boolean
    suspend fun complete(response: RequestResponse)
}

/**
 * Bucket 标识
 */
@JvmInline
value class BucketKey(val value: String)

/**
 * 限速信息
 */
data class RateLimit(val limit: Long, val remaining: Long) {
    val isExhausted: Boolean get() = remaining <= 0
}

/**
 * Rate limit 响应分类
 */
sealed class RequestResponse {
    abstract val bucketKey: BucketKey?
    abstract val rateLimit: RateLimit?
    abstract val resetSeconds: Long?

    data object Error : RequestResponse() {
        override val bucketKey: BucketKey? = null
        override val rateLimit: RateLimit? = null
        override val resetSeconds: Long? = null
    }

    data class Accepted(
        override val bucketKey: BucketKey?,
        override val rateLimit: RateLimit?,
        override val resetSeconds: Long?,
    ) : RequestResponse()

    data class GlobalRateLimit(
        override val bucketKey: BucketKey?,
        override val rateLimit: RateLimit?,
        override val resetSeconds: Long?,
    ) : RequestResponse()

    data class BucketRateLimit(
        override val bucketKey: BucketKey,
        override val rateLimit: RateLimit?,
        override val resetSeconds: Long?,
    ) : RequestResponse()

    companion object {
        /**
         * 从 HTTP 响应头构建 [RequestResponse]
         *
         * 适配 KOOK 的 X-Rate-Limit-* 头 (注意有 dash, 与 Discord 不同)
         */
        fun from(response: HttpResponse): RequestResponse {
            val bucket = response.rateLimitBucket?.let { BucketKey(it) }
            val rateLimit = run {
                val limit = response.rateLimitLimit ?: return@run null
                val remaining = response.rateLimitRemaining ?: return@run null
                RateLimit(limit, remaining)
            }
            val resetSeconds = response.rateLimitResetSeconds

            return when {
                response.isGlobalRateLimit -> GlobalRateLimit(bucket, rateLimit, resetSeconds)
                response.isRateLimit -> BucketRateLimit(
                    bucket ?: BucketKey("unknown"),
                    rateLimit,
                    resetSeconds,
                )
                response.isError -> Error
                else -> Accepted(bucket, rateLimit, resetSeconds)
            }
        }
    }
}
