package com.github.hank9999.kook.rest.exception

/**
 * KOOK REST API 请求异常
 *
 * 覆盖两种错误场景:
 * - HTTP 层错误 (4xx/5xx): [kookCode] 为 null
 * - 业务错误 (HTTP 200 但 code != 0): [kookCode] 为非零值
 *
 * @param status HTTP 状态码
 * @param kookCode KOOK 业务错误码 (code 字段), HTTP 层错误时为 null
 * @param kookMessage KOOK 错误消息或 HTTP 响应体
 */
class KookRestRequestException(
    val status: Int,
    val kookCode: Int?,
    val kookMessage: String,
) : RuntimeException(formatMessage(status, kookCode, kookMessage)) {

    val isBusinessError: Boolean get() = kookCode != null && kookCode != 0

    companion object {
        private fun formatMessage(status: Int, kookCode: Int?, kookMessage: String): String {
            return if (kookCode != null) {
                "KOOK API error: HTTP $status, code=$kookCode, message=$kookMessage"
            } else {
                "HTTP error: $status, body=$kookMessage"
            }
        }
    }
}
