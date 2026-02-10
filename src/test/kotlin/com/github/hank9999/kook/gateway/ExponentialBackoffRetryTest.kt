package com.github.hank9999.kook.gateway

import com.github.hank9999.kook.gateway.retry.ExponentialBackoffRetry
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

/** ExponentialBackoffRetry 指数退避重试策略测试 */
class ExponentialBackoffRetryTest {

    /** 验证 hasNext 在达到最大重试次数后返回 false */
    @Test
    fun retryHasNextRespectsMaxTries() = runTest {
        val retry = ExponentialBackoffRetry(
            firstBackoff = 1.milliseconds,
            maxBackoff = 4.milliseconds,
            maxTries = 2,
        )

        assertTrue(retry.hasNext)
        retry.retry()
        assertTrue(retry.hasNext)
        retry.retry()
        assertFalse(retry.hasNext)
    }

    /** 验证 reset 后 hasNext 恢复为 true */
    @Test
    fun retryResetRestoresHasNext() = runTest {
        val retry = ExponentialBackoffRetry(
            firstBackoff = 1.milliseconds,
            maxBackoff = 4.milliseconds,
            maxTries = 1,
        )

        retry.retry()
        assertFalse(retry.hasNext)

        retry.reset()
        assertTrue(retry.hasNext)
    }
}

