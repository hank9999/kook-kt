package com.github.hank9999.kook.rest.service

import com.github.hank9999.kook.rest.request.KtorRequestHandler
import com.github.hank9999.kook.rest.request.RequestHandler
import java.io.Closeable

/**
 * KOOK REST API 客户端门面
 *
 * 聚合所有领域 Service, 提供统一入口。
 *
 * ```kotlin
 * val rest = RestClient("Bot token")
 * val me = rest.user.getMe()
 * ```
 */
class RestClient(requestHandler: RequestHandler) : RestService(requestHandler), Closeable {
    override fun close() {
        (requestHandler as? Closeable)?.close()
    }
}

/**
 * 便捷工厂函数
 */
fun RestClient(token: String): RestClient {
    val handler = KtorRequestHandler.create(token)
    return RestClient(handler)
}
