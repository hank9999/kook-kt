package com.github.hank9999.kook.gateway

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import kotlinx.serialization.Serializable

/**
 * Gateway API 客户端
 * 负责通过 KOOK HTTP API 获取 WebSocket 网关地址
 *
 * @param token Bot Token，用于鉴权
 * @param client Ktor HTTP 客户端实例
 * @param baseUrl API 基础地址
 */
internal class GatewayApiClient(
    private val token: String,
    private val client: HttpClient,
    private val baseUrl: String = "https://www.kookapp.cn",
) {

    /**
     * 获取 WebSocket 网关连接地址
     *
     * @param compress 是否启用 zlib 压缩，默认为 true
     * @return 可用的 WebSocket 网关 URL
     * @throws IllegalStateException 当 API 返回非零状态码时
     */
    suspend fun getGatewayUrl(compress: Boolean = true): String {
        val response: ApiResponse<GatewayData> = client.get("$baseUrl/api/v3/gateway/index") {
            header(HttpHeaders.Authorization, "Bot $token")
            parameter("compress", if (compress) 1 else 0)
        }.body()

        check(response.code == 0) {
            "Failed to get gateway url: code=${response.code}, message=${response.message}"
        }
        return response.data.url
    }

    /** API 通用响应结构 */
    @Serializable
    private data class ApiResponse<T>(
        val code: Int,
        val message: String,
        val data: T,
    )

    /** 网关数据，包含 WebSocket 连接 URL */
    @Serializable
    private data class GatewayData(
        val url: String,
    )
}

