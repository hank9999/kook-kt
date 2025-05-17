package com.github.hank9999.kook.gateway

import com.github.hank9999.kook.common.utils.CryptUtils
import com.github.hank9999.kook.common.utils.JSON
import com.github.hank9999.kook.common.utils.Zlib
import com.github.hank9999.kook.common.utils.JSON.String
import com.github.hank9999.kook.common.utils.JSON.get
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.EmbeddedServer
import io.ktor.server.request.receiveStream
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlin.coroutines.CoroutineContext

class WebHookGateway : IGateway {
    private val logger = KotlinLogging.logger {}

    constructor(
        dispatcher: CoroutineContext,
        events: MutableSharedFlow<Event>,
        server: EmbeddedServer<ApplicationEngine, ApplicationEngine.Configuration>,
        path: String = "/kook-kt/webhook",
        compress: Boolean = true,
        verifyToken: String = "",
        encryptKey: String = ""
    ) {
        this.coroutineContext = SupervisorJob() + dispatcher
        this.events = events
        this.server = server
        this.path = path
        this.compress = compress
        this.verifyToken = verifyToken
        this.encryptKey = encryptKey
    }

    private val server: EmbeddedServer<ApplicationEngine, ApplicationEngine.Configuration>
    override val coroutineContext: CoroutineContext
    override val events: SharedFlow<Event>
    private val path: String
    private val compress: Boolean
    private val verifyToken: String
    private val encryptKey: String
    val json = JSON.defaultJson()

    fun initWebhook() {
        logger.debug {
            "Webhook: 开始初始化路由, 路径: $path, 压缩: $compress, " +
            "来源验证: ${if (verifyToken.isNotEmpty()) '有' else '无'}, " +
            "加密: ${if (encryptKey.isNotEmpty()) '有' else '无'}"
        }
        server.application.routing {
            post(path) {
                val byteContent = call.receiveStream().readBytes()
                val textContent = if (compress) Zlib.decompress(byteContent) else byteContent.decodeToString()
                logger.debug { "Webhook: 收到原始消息 $textContent" }
                val rawElement = json.parseToJsonElement(textContent)
                val element = if (encryptKey.isNotEmpty()) {
                    val encryptData = rawElement["encrypt"]?.String ?: run {
                        logger.warn { "Webhook: 未找到加密数据" }
                        call.respond(HttpStatusCode.OK)
                        return@post
                    }
                    val decryptJson = CryptUtils.decrypt(encryptData, encryptKey).also {
                        logger.debug { "Webhook: 解密后内容 $it" }
                    }
                    json.parseToJsonElement(decryptJson)
                } else {
                    rawElement
                }
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}