package com.github.hank9999.kook.gateway

import com.github.hank9999.kook.common.utils.JSON
import com.github.hank9999.kook.common.utils.Zlib
import com.github.hank9999.kook.gateway.entity.Event
import com.github.hank9999.kook.gateway.entity.ReconnectPayload
import com.github.hank9999.kook.gateway.handler.HeartbeatHandler
import com.github.hank9999.kook.gateway.handler.HandshakeHandler
import com.github.hank9999.kook.gateway.handler.ReconnectHandler
import com.github.hank9999.kook.gateway.handler.SequenceHandler
import com.github.hank9999.kook.gateway.retry.ExponentialBackoffRetry
import com.github.hank9999.kook.gateway.retry.Retry
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.url
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.takeWhile
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import io.ktor.websocket.readText
import io.ktor.websocket.close
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.selects.select
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.logger {}

/**
 * WebSocket 网关实现
 * 通过 KOOK WebSocket 协议建立与服务端的长连接，处理握手、心跳、事件接收和断线重连。
 * 支持 Resume（恢复）和 Full（全量）两种重连模式
 *
 * @param token Bot Token
 * @param compress 是否启用 zlib 压缩
 * @param dispatcher 协程调度器
 * @param eventFlow 事件分发流
 * @param client Ktor HTTP 客户端
 * @param apiBaseUrl API 基础地址
 */
public class WebSocketGateway(
    private val token: String,
    private val compress: Boolean = true,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
    eventFlow: MutableSharedFlow<Event> = MutableSharedFlow(extraBufferCapacity = 1024),
    private val client: HttpClient = defaultClient(),
    private val apiBaseUrl: String = "https://www.kookapp.cn",
) : IGateway {

    /** 网关内部状态 */
    private sealed class State {
        object Stopped : State()
        object Running : State()
    }

    /** 重连模式 */
    private enum class ReconnectMode {
        /** 无需重连 */
        NONE,
        /** 恢复重连，携带 sessionId 和 sn 恢复会话 */
        RESUME,
        /** 全量重连，丢弃会话状态重新建立连接 */
        FULL,
    }

    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatcher

    private val eventFlowRef = eventFlow
    override val events: SharedFlow<Event> = eventFlow.asSharedFlow()

    private val _ping = MutableStateFlow<Duration?>(null)
    override val ping: StateFlow<Duration?> = _ping.asStateFlow()

    private val apiClient = GatewayApiClient(token, client, apiBaseUrl)
    private val stateMutex = Mutex()

    private val sequenceHandler = SequenceHandler()
    private val handshakeHandler = HandshakeHandler(events)
    private val reconnectHandler: ReconnectHandler
    private val heartbeatHandler: HeartbeatHandler

    private var socket: DefaultClientWebSocketSession? = null
    private var reconnectSignal = CompletableDeferred<Unit>()

    @Volatile
    private var state: State = State.Stopped
    private var reconnectMode: ReconnectMode = ReconnectMode.NONE

    /** 主循环重试策略（指数退避 2s~60s，无限重试） */
    private val runningRetry: Retry = ExponentialBackoffRetry(
        firstBackoff = 2.seconds,
        maxBackoff = 60.seconds,
        maxTries = Int.MAX_VALUE,
    )

    /** Resume 重连重试策略（指数退避 8s~16s，最多 2 次） */
    private val resumeRetry: Retry = ExponentialBackoffRetry(
        firstBackoff = 8.seconds,
        maxBackoff = 16.seconds,
        maxTries = 2,
    )

    init {
        reconnectHandler = ReconnectHandler(events) { event ->
            val payload = event.data?.let {
                JSON.defaultJson().decodeFromJsonElement<ReconnectPayload>(it)
            }
            logger.info { "received reconnect signal from server (code=${payload?.code}, err=${payload?.err})" }
            requestReconnect(fullReset = true)
        }

        heartbeatHandler = HeartbeatHandler(
            events,
            sendPing = { sn -> sendSignal(2, sn) },
            onTimeout = {
                logger.warn { "heartbeat timeout, switching to resume reconnect" }
                requestReconnect(fullReset = false)
            },
            onPingUpdate = { _ping.value = it },
            sequence = sequenceHandler,
        )
    }

    /**
     * 启动网关主循环
     * 持续尝试连接 WebSocket，完成握手后接收事件，断连后根据重连模式自动恢复
     */
    override suspend fun start(): Unit = withContext(Dispatchers.Default) {
        prepareStart()

        while (runningRetry.hasNext && isRunning()) {
            val attemptMode = prepareAttemptMode()
            val handshakeAwaiter = handshakeHandler.newHelloAwaiter()
            reconnectSignal = CompletableDeferred()

            try {
                val gatewayUrl = buildGatewayUrl(resume = attemptMode == ReconnectMode.RESUME)
                socket = client.webSocketSession { url(gatewayUrl) }

                coroutineScope {
                    // 启动消息读取循环
                    val readJob = launch {
                        readSocketLoop()
                    }

                    // 等待握手完成
                    val handshakeOk = waitHandshake(handshakeAwaiter)
                    if (!handshakeOk) {
                        throw IllegalStateException("Handshake failed or timeout")
                    }

                    // 握手成功，重置重试计数
                    runningRetry.reset()
                    if (attemptMode == ReconnectMode.RESUME) {
                        resumeRetry.reset()
                    }
                    setReconnectMode(ReconnectMode.NONE)

                    // 阻塞直到收到重连信号
                    reconnectSignal.await()
                    readJob.join()
                }
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                logger.error(exception) { "gateway loop error" }
                if (!isRunning()) break

                if (attemptMode == ReconnectMode.RESUME) {
                    // Resume 模式失败，先退避再保持 RESUME 模式下次重试
                    if (resumeRetry.hasNext) {
                        resumeRetry.retry()
                    }
                    ensureReconnectMode(ReconnectMode.RESUME)
                } else {
                    // 普通连接失败，执行退避重试
                    runningRetry.retry()
                }
            } finally {
                closeSocket()
            }
        }

        _ping.value = null
        cancelHandlers()
        closeSocket()
    }

    /** 停止网关，关闭连接并清理状态 */
    override suspend fun stop() {
        stateMutex.withLock {
            state = State.Stopped
            reconnectMode = ReconnectMode.NONE
        }
        cancelHandlers()
        _ping.value = null
        reconnectSignal.complete(Unit)
        closeSocket()
    }

    /** 取消所有事件处理器的协程作用域 */
    private fun cancelHandlers() {
        handshakeHandler.cancel()
        heartbeatHandler.cancel()
        reconnectHandler.cancel()
    }

    /** 初始化启动状态，重置所有处理器和重试策略 */
    private suspend fun prepareStart() {
        stateMutex.withLock {
            check(state !is State.Running) { "Gateway is already running" }
            state = State.Running
            reconnectMode = ReconnectMode.NONE
        }

        sequenceHandler.reset()
        handshakeHandler.clearSession()
        runningRetry.reset()
        resumeRetry.reset()
        reconnectSignal = CompletableDeferred()
        _ping.value = null

        handshakeHandler.attach()
        heartbeatHandler.attach()
        reconnectHandler.attach()
    }

    /**
     * 准备本次连接尝试的模式
     * 根据当前重连模式决定：FULL 模式下重置会话、RESUME 模式下尝试恢复（超出重试次数则降级为全量重连）
     *
     * @return 本次尝试应使用的重连模式
     */
    private suspend fun prepareAttemptMode(): ReconnectMode {
        val currentMode = currentReconnectMode()
        if (currentMode == ReconnectMode.FULL) {
            resetSessionState()
            setReconnectMode(ReconnectMode.NONE)
            return ReconnectMode.NONE
        }

        if (currentMode == ReconnectMode.RESUME) {
            if (resumeRetry.hasNext) {
                return ReconnectMode.RESUME
            }

            resetSessionState()
            setReconnectMode(ReconnectMode.NONE)
            return ReconnectMode.NONE
        }

        return ReconnectMode.NONE
    }

    /**
     * 构建网关连接 URL
     * Resume 模式下在 URL 中附加 sessionId 和序列号参数
     *
     * @param resume 是否为恢复连接模式
     * @return 完整的网关 WebSocket URL
     */
    private suspend fun buildGatewayUrl(resume: Boolean): String {
        val gatewayUrl = apiClient.getGatewayUrl(compress = compress)
        if (!resume) return gatewayUrl

        val sessionId = handshakeHandler.sessionId ?: return gatewayUrl
        val separator = if (gatewayUrl.contains("?")) "&" else "?"
        return "$gatewayUrl${separator}resume=1&sn=${sequenceHandler.value}&session_id=$sessionId"
    }

    /**
     * 等待握手完成
     * 同时监听握手结果和重连信号，6 秒超时返回 false
     */
    private suspend fun waitHandshake(awaiter: CompletableDeferred<Boolean>): Boolean {
        return withTimeoutOrNull(6.seconds) {
            select {
                awaiter.onAwait { result ->
                    result
                }
                reconnectSignal.onAwait {
                    false
                }
            }
        } ?: false
    }

    /** WebSocket 消息读取循环，根据帧类型分发处理，连接断开时标记 Resume 重连 */
    private suspend fun readSocketLoop() {
        val currentSocket = socket ?: return

        try {
            currentSocket.incoming.receiveAsFlow()
                .buffer(Channel.UNLIMITED)
                .takeWhile { frame ->
                    frame !is Frame.Close && isRunning() && !reconnectSignal.isCompleted
                }
                .collect { frame ->
                    when (frame) {
                        is Frame.Text -> handleRawText(frame.readText())
                        is Frame.Binary -> handleRawText(Zlib.decompress(frame.readBytes()))
                        else -> Unit
                    }
                }
        } finally {
            if (isRunning() && !reconnectSignal.isCompleted) {
                markReconnect(ReconnectMode.RESUME, closeSocket = false)
            }
        }
    }

    /** 解析原始文本为事件对象，经序列号处理器排序后分发到事件流 */
    private suspend fun handleRawText(raw: String) {
        val event = JSON.defaultJson().decodeFromString(Event.EventSerializer, raw)
        when (val result = sequenceHandler.process(event)) {
            is SequenceHandler.ProcessResult.Events -> {
                for (ordered in result.events) {
                    eventFlowRef.emit(ordered)
                }
            }
            is SequenceHandler.ProcessResult.BufferOverflow -> {
                logger.warn { "sequence buffer overflow (bufferLimit exceeded), requesting resume" }
                requestReconnect(fullReset = false)
            }
        }
    }

    /**
     * 请求重连
     *
     * @param fullReset 是否全量重连，true 为 FULL 模式，false 为 RESUME 模式
     */
    private suspend fun requestReconnect(fullReset: Boolean) {
        val mode = if (fullReset) ReconnectMode.FULL else ReconnectMode.RESUME
        markReconnect(mode, closeSocket = true)
    }

    /**
     * 标记重连并触发信号
     * FULL 模式优先级高于 RESUME，一旦标记为 FULL 不会被降级
     */
    private suspend fun markReconnect(mode: ReconnectMode, closeSocket: Boolean) {
        val shouldReconnect = stateMutex.withLock {
            if (state !is State.Running) {
                false
            } else {
                reconnectMode = when {
                    reconnectMode == ReconnectMode.FULL -> ReconnectMode.FULL
                    mode == ReconnectMode.FULL -> ReconnectMode.FULL
                    else -> ReconnectMode.RESUME
                }
                true
            }
        }

        if (!shouldReconnect) return

        reconnectSignal.complete(Unit)
        if (closeSocket) {
            closeSocket()
        }
    }

    /** 重置会话状态，清除序列号、sessionId 和 Resume 重试计数 */
    private suspend fun resetSessionState() {
        sequenceHandler.reset()
        handshakeHandler.clearSession()
        resumeRetry.reset()
        _ping.value = null
    }

    /** 仅在当前无重连模式时设置指定模式，避免覆盖已有的重连请求 */
    private suspend fun ensureReconnectMode(mode: ReconnectMode) {
        stateMutex.withLock {
            if (state is State.Running && reconnectMode == ReconnectMode.NONE) {
                reconnectMode = mode
            }
        }
    }

    /** 获取当前重连模式（线程安全） */
    private suspend fun currentReconnectMode(): ReconnectMode {
        return stateMutex.withLock { reconnectMode }
    }

    /** 设置重连模式（线程安全） */
    private suspend fun setReconnectMode(mode: ReconnectMode) {
        stateMutex.withLock {
            reconnectMode = mode
        }
    }

    /** 检查网关是否处于运行状态（volatile 读，无锁） */
    private fun isRunning(): Boolean {
        return state is State.Running
    }

    /** 发送信令帧，包含信令类型和序列号 */
    private suspend fun sendSignal(signal: Int, sn: Int) {
        sendText("{\"s\":$signal,\"sn\":$sn}")
    }

    /** 发送文本帧到 WebSocket */
    private suspend fun sendText(text: String) {
        socket?.send(Frame.Text(text))
    }

    /** 关闭当前 WebSocket 连接，忽略关闭过程中的异常 */
    private suspend fun closeSocket() {
        val currentSocket = socket ?: return
        socket = null
        runCatching {
            currentSocket.close(CloseReason(1000, "closing"))
        }
    }

    private companion object {
        /** 创建默认的 HTTP 客户端，安装 WebSocket 和 JSON 内容协商插件 */
        private fun defaultClient(): HttpClient {
            return HttpClient(OkHttp) {
                install(WebSockets)
                install(ContentNegotiation) {
                    json(JSON.defaultJson())
                }
            }
        }
    }
}
