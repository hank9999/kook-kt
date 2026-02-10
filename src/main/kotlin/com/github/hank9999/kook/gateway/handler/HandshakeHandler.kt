package com.github.hank9999.kook.gateway.handler

import com.github.hank9999.kook.common.utils.JSON.Int
import com.github.hank9999.kook.common.utils.JSON.String
import com.github.hank9999.kook.common.utils.JSON.get
import com.github.hank9999.kook.gateway.entity.Event
import com.github.hank9999.kook.gateway.entity.HandshakeAckEvent
import com.github.hank9999.kook.gateway.entity.ResumeAckEvent
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/**
 * 握手处理器
 * 监听 Hello 事件（信令 1），完成 WebSocket 握手确认并提取 sessionId
 */
internal class HandshakeHandler(
    flow: Flow<Event>,
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : Handler(flow, "HandshakeHandler", dispatcher) {

    /** 当前会话 ID，握手成功后由服务端返回 */
    var sessionId: String? = null
        private set

    private var helloDeferred: CompletableDeferred<Boolean>? = null

    override fun start() {
        on<HandshakeAckEvent> { event ->
            val hello = helloDeferred ?: return@on
            val code = event.data?.get("code")?.Int

            if (code == 0) {
                // 握手成功，记录 sessionId
                sessionId = event.data?.get("session_id")?.String
                hello.complete(true)
            } else {
                // 握手失败
                hello.complete(false)
            }
        }

        on<ResumeAckEvent> { event ->
            val hello = helloDeferred ?: return@on
            // ResumeAck 携带 session_id 时更新，收到即代表恢复成功
            event.data?.get("session_id")?.String?.let { sessionId = it }
            hello.complete(true)
        }
    }

    /**
     * 创建新的握手等待器
     *
     * @return 一个 [CompletableDeferred]，握手成功时完成为 true，失败时为 false
     */
    fun newHelloAwaiter(): CompletableDeferred<Boolean> {
        return CompletableDeferred<Boolean>().also { helloDeferred = it }
    }

    /** 清除当前会话 ID，用于全量重连前重置状态 */
    fun clearSession() {
        sessionId = null
    }
}

