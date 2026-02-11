package com.github.hank9999.kook.rest.exception

import com.github.hank9999.kook.rest.request.Request

/**
 * KOOK REST API 请求异常
 *
 * 覆盖两种错误场景:
 * - HTTP 层错误 (4xx/5xx): [kookCode] 为 null
 * - 业务错误 (HTTP 200 但 code != 0): [kookCode] 为非零值
 *
 * @param request 触发异常的原始请求
 * @param status HTTP 状态码
 * @param kookCode KOOK 业务错误码 (code 字段), HTTP 层错误时为 null
 * @param kookMessage KOOK 错误消息或 HTTP 响应体
 * @param cause 可选的异常原因链
 */
class KookRestRequestException(
    val request: Request<*>,
    val status: Int,
    val kookCode: Int?,
    val kookMessage: String,
    cause: Throwable? = null,
) : RuntimeException(formatMessage(request, status, kookCode, kookMessage), cause) {

    val isBusinessError: Boolean get() = kookCode != null && kookCode != 0

    companion object {
        private fun formatMessage(request: Request<*>, status: Int, kookCode: Int?, kookMessage: String): String {
            val route = request.route
            return if (kookCode != null) {
                "KOOK API error: ${route.method.value} ${route.path} HTTP $status, code=$kookCode, message=$kookMessage"
            } else {
                "HTTP error: ${route.method.value} ${route.path} $status, body=$kookMessage"
            }
        }
    }
}
