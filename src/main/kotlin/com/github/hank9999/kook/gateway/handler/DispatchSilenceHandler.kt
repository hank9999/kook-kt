package com.github.hank9999.kook.gateway.handler

import com.github.hank9999.kook.common.utils.JSON.Int
import com.github.hank9999.kook.common.utils.JSON.get
import com.github.hank9999.kook.gateway.entity.Event
import com.github.hank9999.kook.gateway.entity.HandshakeAckEvent
import com.github.hank9999.kook.gateway.entity.MessageEvent
import com.github.hank9999.kook.gateway.entity.ResumeAckEvent
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration

private val logger = KotlinLogging.logger {}

/**
 * Dispatch 静默检测处理器
 *
 * 监控 dispatch 事件（信令 0）的流动性。握手/恢复成功后开始计时，
 * 若在 [baseSilenceTimeout] 内未收到任何 dispatch 事件，触发全量重连。
 *
 * 为避免对真正安静的 bot 造成无限重连循环，每次因静默触发重连后，
 * 下次超时阈值翻倍（指数退避），直到 [maxSilenceTimeout] 封顶。
 * 收到任何 dispatch 事件后重置回初始阈值。
 *
 * @param baseSilenceTimeout 初始静默超时阈值
 * @param maxSilenceTimeout 退避上限
 * @param onSilence 静默超时时的回调（应触发全量重连）
 */
internal class DispatchSilenceHandler(
    flow: Flow<Event>,
    private val baseSilenceTimeout: Duration,
    private val maxSilenceTimeout: Duration,
    private val onSilence: suspend () -> Unit,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : Handler(flow, "DispatchSilenceHandler", dispatcher) {

    private var monitorJob: Job? = null
    private val dispatchSignal = Channel<Unit>(capacity = Channel.CONFLATED)
    @Volatile
    internal var currentTimeout: Duration = baseSilenceTimeout
        private set

    override fun start() {
        on<HandshakeAckEvent> { event ->
            val code = event.data?.get("code")?.Int
            if (code == 0) {
                startMonitoring()
            }
        }

        on<ResumeAckEvent> {
            startMonitoring()
        }

        on<MessageEvent> {
            dispatchSignal.trySend(Unit)
        }
    }

    /**
     * 启动静默监控协程。
     * 每次成功握手/恢复后调用，取消前一轮监控并开始新一轮。
     */
    private fun startMonitoring() {
        monitorJob?.cancel()
        monitorJob = launch(start = CoroutineStart.UNDISPATCHED) {
            while (isActive) {
                val received = withTimeoutOrNull(currentTimeout) {
                    dispatchSignal.receive()
                }
                if (received == null) {
                    logger.warn { "no dispatch events received for $currentTimeout, triggering full reconnect" }
                    currentTimeout = (currentTimeout * 2).coerceAtMost(maxSilenceTimeout)
                    onSilence()
                    return@launch
                }
                // 收到 dispatch 事件，重置退避
                currentTimeout = baseSilenceTimeout
            }
        }
    }

    /** 停止监控，清除状态 */
    fun stop() {
        monitorJob?.cancel()
        monitorJob = null
    }

    override fun cancel() {
        super.cancel()
        monitorJob = null
        currentTimeout = baseSilenceTimeout
    }
}
