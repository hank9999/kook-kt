package com.github.hank9999.kook.gateway

import com.github.hank9999.kook.common.utils.JSON
import com.github.hank9999.kook.gateway.entity.Event
import com.github.hank9999.kook.gateway.handler.HandshakeHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/** HandshakeHandler 握手处理器测试 */
@OptIn(ExperimentalCoroutinesApi::class)
class HandshakeHandlerTest {

    private val json = JSON.defaultJson()

    /** 验证收到 code=0 的 Hello 事件时握手成功并正确提取 sessionId */
    @Test
    fun handshakeHandlerParsesSuccessHello() = runTest {
        val flow = MutableSharedFlow<Event>(replay = 1)
        val handler = HandshakeHandler(flow, UnconfinedTestDispatcher(testScheduler))
        handler.attach()

        val awaiter = handler.newHelloAwaiter()
        flow.emit(json.decodeFromString("""{"s":1,"d":{"code":0,"session_id":"abc123"}}"""))

        assertTrue(withTimeout(2.seconds) { awaiter.await() })
        assertEquals("abc123", handler.sessionId)
    }

    /** 验证收到非零 code 的 Hello 事件时握手失败且 sessionId 为 null */
    @Test
    fun handshakeHandlerParsesFailedHello() = runTest {
        val flow = MutableSharedFlow<Event>(replay = 1)
        val handler = HandshakeHandler(flow, UnconfinedTestDispatcher(testScheduler))
        handler.attach()

        val awaiter = handler.newHelloAwaiter()
        flow.emit(json.decodeFromString("""{"s":1,"d":{"code":40103}}"""))

        assertFalse(withTimeout(2.seconds) { awaiter.await() })
        assertNull(handler.sessionId)
    }

    /** 验证 clearSession 能正确清除已保存的 sessionId */
    @Test
    fun handshakeHandlerCanClearSession() = runTest {
        val flow = MutableSharedFlow<Event>(replay = 1)
        val handler = HandshakeHandler(flow, UnconfinedTestDispatcher(testScheduler))
        handler.attach()

        val awaiter = handler.newHelloAwaiter()
        flow.emit(json.decodeFromString("""{"s":1,"d":{"code":0,"session_id":"to-clear"}}"""))
        assertTrue(withTimeout(2.seconds) { awaiter.await() })
        assertEquals("to-clear", handler.sessionId)

        handler.clearSession()
        assertNull(handler.sessionId)
    }
}
