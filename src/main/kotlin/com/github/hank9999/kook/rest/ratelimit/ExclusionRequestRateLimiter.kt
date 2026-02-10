package com.github.hank9999.kook.rest.ratelimit

import com.github.hank9999.kook.rest.request.Request
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.sync.Mutex

/**
 * 串行请求限速器
 *
 * 所有请求排队执行, 最大程度避免触发 429。
 */
class ExclusionRequestRateLimiter : AbstractRateLimiter() {
    override val logger: KLogger = KotlinLogging.logger {}
    private val sequentialLock = Mutex()

    override suspend fun await(request: Request<*>): RequestToken {
        sequentialLock.lock()
        return super.await(request)
    }

    override fun newToken(request: Request<*>, bucketKey: BucketKey?): RequestToken {
        return ExclusionRequestToken(request, bucketKey)
    }

    private inner class ExclusionRequestToken(
        request: Request<*>,
        bucketKey: BucketKey?,
    ) : AbstractRequestToken(request, bucketKey) {
        override suspend fun complete(response: RequestResponse) {
            try {
                super.complete(response)
            } finally {
                sequentialLock.unlock()
            }
        }
    }
}
