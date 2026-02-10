package com.github.hank9999.kook.rest.request

/**
 * KOOK API 请求处理器接口
 *
 * 职责:
 * 1. 注入认证头
 * 2. 遵循 Rate Limit
 * 3. 拆包 KookApiResponse, 检查 code
 * 4. 反序列化 data 字段
 * 5. 处理 429 重试
 */
interface RequestHandler {
    val token: String

    /**
     * 执行请求, 返回解析后的响应 data
     *
     * @throws com.github.hank9999.kook.rest.exception.KookRestRequestException 当 code != 0 或 HTTP 错误时
     */
    suspend fun <T> handle(request: Request<T>): T

    /**
     * 拦截 RequestBuilder, 注入公共头 (Authorization 等)
     */
    suspend fun <T> intercept(builder: RequestBuilder<T>) {
        builder.header("Authorization", "Bot $token")
    }
}
