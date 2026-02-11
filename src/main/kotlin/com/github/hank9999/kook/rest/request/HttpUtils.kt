package com.github.hank9999.kook.rest.request

import io.ktor.client.statement.HttpResponse

// KOOK Rate Limit 响应头 (注意 Rate-Limit 之间有 dash, 与 Discord 的 RateLimit 不同)
private const val RATE_LIMIT_LIMIT = "X-Rate-Limit-Limit"
private const val RATE_LIMIT_REMAINING = "X-Rate-Limit-Remaining"
private const val RATE_LIMIT_RESET = "X-Rate-Limit-Reset"
private const val RATE_LIMIT_BUCKET = "X-Rate-Limit-Bucket"
private const val RATE_LIMIT_GLOBAL = "X-Rate-Limit-Global"

val HttpResponse.isRateLimit: Boolean get() = status.value == 429

/** 502 Bad Gateway / 503 Service Unavailable / 504 Gateway Timeout */
val HttpResponse.isTransientError: Boolean get() = status.value in intArrayOf(502, 503, 504)
val HttpResponse.isError: Boolean get() = status.value in 400..<600
val HttpResponse.isGlobalRateLimit: Boolean get() = headers[RATE_LIMIT_GLOBAL] != null

val HttpResponse.rateLimitLimit: Long? get() = headers[RATE_LIMIT_LIMIT]?.toLongOrNull()
val HttpResponse.rateLimitRemaining: Long? get() = headers[RATE_LIMIT_REMAINING]?.toLongOrNull()

/** KOOK 的 Reset 是等待秒数, 不是 Unix 时间戳 */
val HttpResponse.rateLimitResetSeconds: Long? get() = headers[RATE_LIMIT_RESET]?.toLongOrNull()
val HttpResponse.rateLimitBucket: String? get() = headers[RATE_LIMIT_BUCKET]

fun HttpResponse.logString(body: String): String =
    "[RESPONSE]:${status.value}:${call.request.method.value}:${call.request.url} body:$body"

fun Request<*>.logString(body: String): String =
    "[REQUEST]:${route.method.value}:${route.path} body:$body"
