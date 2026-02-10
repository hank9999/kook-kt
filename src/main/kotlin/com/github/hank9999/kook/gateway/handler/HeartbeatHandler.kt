package com.github.hank9999.kook.gateway.handler

import com.github.hank9999.kook.common.utils.JSON.Int
import com.github.hank9999.kook.common.utils.JSON.get
import com.github.hank9999.kook.gateway.entity.Event
import com.github.hank9999.kook.gateway.entity.HandshakeAckEvent
import com.github.hank9999.kook.gateway.entity.PongEvent
import com.github.hank9999.kook.gateway.entity.ResumeAckEvent
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource

/**
 * 心跳处理器
 * 握手成功后启动心跳循环，定期发送 Ping（信令 2）并等待 Pong（信令 3）回复。
 * 当心跳超时时触发重连回调
 *
 * @param sendPing 发送 Ping 信令的回调，参数为当前序列号
 * @param onTimeout 心跳超时时的回调
 * @param onPingUpdate 延迟更新回调，参数为本次 Ping-Pong 的往返耗时
 * @param sequence 序列号处理器，用于获取当前序列号
 * @param random 随机数生成器，用于心跳间隔的随机抖动
 */
internal class HeartbeatHandler(
    flow: Flow<Event>,
    private val sendPing: suspend (sn: Int) -> Unit,
    private val onTimeout: suspend () -> Unit,
    private val onPingUpdate: (Duration) -> Unit,
    private val sequence: SequenceHandler,
    private val random: Random = Random.Default,
) : Handler(flow, "HeartbeatHandler") {

    private var heartbeatJob: Job? = null
    private val pongSignal = Channel<Unit>(capacity = Channel.CONFLATED)
    private var pendingPingAt: TimeMark? = null

    override fun start() {
        // 监听握手确认事件，成功则启动心跳循环，失败则停止
        on<HandshakeAckEvent> { event ->
            val code = event.data?.get("code")?.Int
            if (code == 0) {
                startHeartbeatLoop()
            } else {
                stop()
            }
        }

        // 监听 Resume 确认事件，恢复成功后启动心跳循环
        on<ResumeAckEvent> {
            startHeartbeatLoop()
        }

        // 监听 Pong 事件，计算往返延迟并通知信号通道
        on<PongEvent> {
            pendingPingAt?.let { sentAt ->
                onPingUpdate(sentAt.elapsedNow())
                pendingPingAt = null
            }
            pongSignal.trySend(Unit)
        }
    }

    /** 启动心跳循环，以 30±5 秒的随机间隔发送 Ping，超时则最多重试两次 */
    private fun startHeartbeatLoop() {
        heartbeatJob?.cancel()
        heartbeatJob = launch(start = CoroutineStart.UNDISPATCHED) {
            while (isActive) {
                // 心跳间隔为 30 秒加 [-5, 5] 的随机抖动，避免多实例同步发送
                val interval = (30 + random.nextInt(-5, 6)).seconds
                kotlinx.coroutines.delay(interval)

                val pong = sendAndWaitPong()
                if (pong == null) {
                    // 首次超时，等待 2 秒后重试
                    val firstRetry = retryPing(2.seconds)
                    if (!firstRetry) {
                        // 再次超时，等待 4 秒后最终重试
                        val secondRetry = retryPing(4.seconds)
                        if (!secondRetry) {
                            onTimeout()
                            return@launch
                        }
                    }
                }
            }
        }
    }

    /**
     * 延迟后重试发送 Ping
     *
     * @param delayBeforeRetry 重试前的等待时长
     * @return 是否收到 Pong 回复
     */
    private suspend fun retryPing(delayBeforeRetry: Duration): Boolean {
        kotlinx.coroutines.delay(delayBeforeRetry)
        return sendAndWaitPong() != null
    }

    /**
     * 发送 Ping 并等待 Pong 回复
     *
     * @return 收到 Pong 时返回 [Unit]，超时返回 null
     */
    private suspend fun sendAndWaitPong(): Unit? {
        val sn = sequence.value
        pendingPingAt = TimeSource.Monotonic.markNow()
        sendPing(sn)
        return withTimeoutOrNull(6.seconds) {
            pongSignal.receive()
        }
    }

    /** 停止心跳循环，取消定时任务并清除状态 */
    fun stop() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        pendingPingAt = null
    }
}
