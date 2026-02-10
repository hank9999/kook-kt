package com.github.hank9999.kook.gateway.retry

import kotlinx.coroutines.delay
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration

/**
 * 指数退避重试策略
 * 每次重试的等待时长按指数增长（2倍），直到达到上限 [maxBackoff]
 *
 * @param firstBackoff 首次重试的退避时长
 * @param maxBackoff 退避时长的上限
 * @param maxTries 最大重试次数
 */
public class ExponentialBackoffRetry(
    private val firstBackoff: Duration,
    private val maxBackoff: Duration,
    private val maxTries: Int,
) : Retry {

    init {
        require(firstBackoff.isPositive()) { "firstBackoff must be positive, but was $firstBackoff" }
        require(maxBackoff.isPositive()) { "maxBackoff must be positive, but was $maxBackoff" }
        require(maxBackoff >= firstBackoff) {
            "maxBackoff must be greater than or equal to firstBackoff, but was firstBackoff=$firstBackoff, maxBackoff=$maxBackoff"
        }
        require(maxTries > 0) { "maxTries must be greater than 0, but was $maxTries" }
    }

    private val tries = AtomicInteger(0)

    override val hasNext: Boolean
        get() = tries.get() < maxTries

    override fun reset() {
        tries.set(0)
    }

    override suspend fun retry() {
        if (!hasNext) error("max retries exceeded")
        // 原子递增计数器，根据当前重试次数计算退避时长并等待
        val currentTry = tries.getAndIncrement()
        delay(computeBackoff(currentTry))
    }

    /** 根据重试次数计算退避时长：firstBackoff * 2^[tryNumber]，不超过 [maxBackoff] */
    private fun computeBackoff(tryNumber: Int): Duration {
        var backoff = firstBackoff
        repeat(tryNumber) { backoff = (backoff * 2).coerceAtMost(maxBackoff) }
        return backoff
    }
}

