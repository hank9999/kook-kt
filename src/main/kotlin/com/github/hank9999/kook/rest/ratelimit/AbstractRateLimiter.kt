package com.github.hank9999.kook.rest.ratelimit

import com.github.hank9999.kook.rest.request.Request
import io.github.oshai.kotlinlogging.KLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * Rate Limiter 抽象基类
 *
 * - 使用 [TimeSource.Monotonic] 计算恢复时刻 (KOOK Reset 是等待秒数)
 * - Bucket 映射从响应头动态学习
 * - 请求标识直接用 [Route.path]
 */
abstract class AbstractRateLimiter : RequestRateLimiter {
    internal abstract val logger: KLogger

    private val timeSource = TimeSource.Monotonic

    /** 全局限速恢复时刻 */
    private val globalResetMark = AtomicReference(timeSource.markNow())

    /** bucket -> 状态 */
    private val buckets = ConcurrentHashMap<BucketKey, BucketState>()

    /** route path -> bucket key (从响应头动态学习) */
    private val routeBuckets = ConcurrentHashMap<String, BucketKey>()

    override suspend fun await(request: Request<*>): RequestToken {
        // 等待全局限速恢复
        awaitMark(globalResetMark.get())

        // 等待 bucket 限速恢复
        val bucketKey = routeBuckets[request.route.path]
        if (bucketKey != null) {
            buckets[bucketKey]?.awaitAndLock()
        }

        return newToken(request, bucketKey)
    }

    internal abstract fun newToken(request: Request<*>, bucketKey: BucketKey?): RequestToken

    private suspend fun awaitMark(mark: TimeMark) {
        val elapsed = mark.elapsedNow()
        if (elapsed.isNegative()) {
            delay(-elapsed)
        }
    }

    internal abstract inner class AbstractRequestToken(
        private val request: Request<*>,
        private val lockedBucketKey: BucketKey?,
    ) : RequestToken {
        private var _completed = false
        override val completed: Boolean get() = _completed

        override suspend fun complete(response: RequestResponse) {
            try {
                val key = response.bucketKey
                if (key != null) {
                    routeBuckets[request.route.path] = key
                }

                when (response) {
                    is RequestResponse.GlobalRateLimit -> {
                        val resetSec = response.resetSeconds ?: 0
                        logger.debug { "[GLOBAL_RATE_LIMIT] wait ${resetSec}s" }
                        globalResetMark.set(timeSource.markNow() + resetSec.seconds)
                    }
                    is RequestResponse.BucketRateLimit -> {
                        val resetSec = response.resetSeconds ?: 0
                        logger.debug { "[BUCKET_RATE_LIMIT] bucket=${response.bucketKey.value} wait ${resetSec}s" }
                        val state = buckets.getOrPut(response.bucketKey) { BucketState() }
                        state.updateReset(timeSource.markNow() + resetSec.seconds)
                    }
                    is RequestResponse.Accepted -> {
                        // remaining == 0 时主动设置等待, 避免下次请求触发 429
                        if (response.rateLimit?.isExhausted == true && key != null) {
                            val resetSec = response.resetSeconds ?: 0
                            val state = buckets.getOrPut(key) { BucketState() }
                            state.updateReset(timeSource.markNow() + resetSec.seconds)
                        }
                    }
                    is RequestResponse.Error -> {}
                }
            } finally {
                _completed = true
                lockedBucketKey?.let { buckets[it]?.unlock() }
            }
        }
    }

    internal inner class BucketState {
        private val mutex = Mutex()
        @Volatile
        private var resetMark: TimeMark = timeSource.markNow()

        suspend fun awaitAndLock() {
            mutex.lock()
            awaitMark(resetMark)
        }

        fun updateReset(mark: TimeMark) {
            resetMark = mark
        }

        fun unlock() {
            try {
                mutex.unlock()
            } catch (_: IllegalStateException) {
                // mutex was not locked
            }
        }
    }
}
