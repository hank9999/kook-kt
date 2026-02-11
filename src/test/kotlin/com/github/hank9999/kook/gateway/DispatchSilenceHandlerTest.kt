package com.github.hank9999.kook.gateway

import com.github.hank9999.kook.common.utils.JSON
import com.github.hank9999.kook.gateway.entity.Event
import com.github.hank9999.kook.gateway.handler.DispatchSilenceHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class DispatchSilenceHandlerTest {

    private val json = JSON.defaultJson()

    /** 最小的 dispatch 事件 JSON（signal=0，UserMessageEvent） */
    private val dispatchJson = """{"s":0,"sn":1,"d":{
        "channel_type":"GROUP","type":9,"target_id":"t","author_id":"a","content":"hi",
        "msg_id":"m","msg_timestamp":0,"nonce":"n",
        "extra":{"type":9,"guild_id":"g","channel_name":"c",
            "mention":[],"mention_all":false,"mention_roles":[],"mention_here":false}
    }}"""

    private val helloJson = """{"s":1,"d":{"code":0,"session_id":"s1"}}"""
    private val resumeAckJson = """{"s":6,"d":{"session_id":"s1"}}"""

    /** 辅助：创建 handler 并 attach */
    private fun createHandler(
        flow: MutableSharedFlow<Event>,
        baseSilenceTimeout: kotlin.time.Duration = 5.minutes,
        maxSilenceTimeout: kotlin.time.Duration = 40.minutes,
        onSilence: suspend () -> Unit,
        dispatcher: kotlinx.coroutines.test.TestDispatcher,
    ): DispatchSilenceHandler {
        return DispatchSilenceHandler(
            flow = flow,
            baseSilenceTimeout = baseSilenceTimeout,
            maxSilenceTimeout = maxSilenceTimeout,
            onSilence = onSilence,
            dispatcher = dispatcher,
        ).also { it.attach() }
    }

    /** 验证握手成功后在超时时间内收到 dispatch 事件不会触发 onSilence */
    @Test
    fun dispatchEventResetsTimer() = runTest {
        val flow = MutableSharedFlow<Event>(replay = 1)
        var silenceTriggered = false
        val handler = createHandler(
            flow, baseSilenceTimeout = 5.minutes,
            onSilence = { silenceTriggered = true },
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        // 握手成功启动监控
        flow.emit(json.decodeFromString(helloJson))
        // 在超时前发送 dispatch 事件
        advanceTimeBy(4.minutes)
        flow.emit(json.decodeFromString(dispatchJson))
        // 再过 4 分钟（总计 8 分钟，但从最后一次 dispatch 起只过了 4 分钟）
        advanceTimeBy(4.minutes)

        assertFalse(silenceTriggered, "dispatch 事件应重置计时器，不应触发 onSilence")
        handler.cancel()
    }

    /** 验证握手成功后超过 silenceTimeout 没有 dispatch 事件会触发 onSilence */
    @Test
    fun silenceTimeoutTriggersCallback() = runTest {
        val flow = MutableSharedFlow<Event>(replay = 1)
        var silenceTriggered = false
        val handler = createHandler(
            flow, baseSilenceTimeout = 5.minutes,
            onSilence = { silenceTriggered = true },
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        flow.emit(json.decodeFromString(helloJson))
        advanceTimeBy(5.minutes + 1.seconds)

        assertTrue(silenceTriggered, "超时后应触发 onSilence")
        handler.cancel()
    }

    /** 验证仅 pong 事件不能重置计时器 */
    @Test
    fun pongEventDoesNotResetTimer() = runTest {
        val flow = MutableSharedFlow<Event>(replay = 1)
        var silenceTriggered = false
        val handler = createHandler(
            flow, baseSilenceTimeout = 5.minutes,
            onSilence = { silenceTriggered = true },
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        flow.emit(json.decodeFromString(helloJson))
        // 中途发送 pong（信令 3），不是 dispatch 事件
        advanceTimeBy(3.minutes)
        flow.emit(json.decodeFromString("""{"s":3}"""))
        advanceTimeBy(2.minutes + 1.seconds)

        assertTrue(silenceTriggered, "pong 不应重置计时器")
        handler.cancel()
    }

    /** 验证指数退避：每次静默触发后 currentTimeout 翻倍 */
    @Test
    fun exponentialBackoffDoublesTimeout() = runTest {
        val flow = MutableSharedFlow<Event>(replay = 1)
        var silenceCount = 0
        val handler = createHandler(
            flow, baseSilenceTimeout = 5.minutes, maxSilenceTimeout = 40.minutes,
            onSilence = { silenceCount++ },
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        // 第一次触发
        flow.emit(json.decodeFromString(helloJson))
        advanceTimeBy(5.minutes + 1.seconds)
        assertEquals(1, silenceCount)
        assertEquals(10.minutes, handler.currentTimeout, "第一次触发后应退避到 10 分钟")

        // 模拟重连后再次握手，第二次触发
        flow.emit(json.decodeFromString(helloJson))
        advanceTimeBy(10.minutes + 1.seconds)
        assertEquals(2, silenceCount)
        assertEquals(20.minutes, handler.currentTimeout, "第二次触发后应退避到 20 分钟")

        handler.cancel()
    }

    /** 验证退避上限不超过 maxSilenceTimeout */
    @Test
    fun backoffCapsAtMaxTimeout() = runTest {
        val flow = MutableSharedFlow<Event>(replay = 1)
        var silenceCount = 0
        val handler = createHandler(
            flow, baseSilenceTimeout = 5.minutes, maxSilenceTimeout = 15.minutes,
            onSilence = { silenceCount++ },
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        // 5min → 触发, backoff to 10min
        flow.emit(json.decodeFromString(helloJson))
        advanceTimeBy(5.minutes + 1.seconds)
        assertEquals(1, silenceCount)
        assertEquals(10.minutes, handler.currentTimeout)

        // 10min → 触发, backoff to 15min (capped)
        flow.emit(json.decodeFromString(helloJson))
        advanceTimeBy(10.minutes + 1.seconds)
        assertEquals(2, silenceCount)
        assertEquals(15.minutes, handler.currentTimeout, "不应超过 maxSilenceTimeout")

        // 15min → 触发, still 15min (capped)
        flow.emit(json.decodeFromString(helloJson))
        advanceTimeBy(15.minutes + 1.seconds)
        assertEquals(3, silenceCount)
        assertEquals(15.minutes, handler.currentTimeout)

        handler.cancel()
    }

    /** 验证收到 dispatch 事件后退避重置回 baseSilenceTimeout */
    @Test
    fun dispatchEventResetsBackoff() = runTest {
        val flow = MutableSharedFlow<Event>(replay = 1)
        var silenceCount = 0
        val handler = createHandler(
            flow, baseSilenceTimeout = 5.minutes, maxSilenceTimeout = 40.minutes,
            onSilence = { silenceCount++ },
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        // 第一次触发，退避到 10min
        flow.emit(json.decodeFromString(helloJson))
        advanceTimeBy(5.minutes + 1.seconds)
        assertEquals(1, silenceCount)
        assertEquals(10.minutes, handler.currentTimeout)

        // 重连后收到 dispatch 事件
        flow.emit(json.decodeFromString(helloJson))
        advanceTimeBy(1.minutes)
        flow.emit(json.decodeFromString(dispatchJson))
        advanceTimeBy(1.seconds)

        assertEquals(5.minutes, handler.currentTimeout, "收到 dispatch 后应重置回 baseSilenceTimeout")
        assertEquals(1, silenceCount, "不应再次触发")
        handler.cancel()
    }

    /** 验证 ResumeAck 也能启动监控 */
    @Test
    fun resumeAckStartsMonitoring() = runTest {
        val flow = MutableSharedFlow<Event>(replay = 1)
        var silenceTriggered = false
        val handler = createHandler(
            flow, baseSilenceTimeout = 5.minutes,
            onSilence = { silenceTriggered = true },
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        flow.emit(json.decodeFromString(resumeAckJson))
        advanceTimeBy(5.minutes + 1.seconds)

        assertTrue(silenceTriggered, "ResumeAck 后也应启动监控")
        handler.cancel()
    }

    /** 验证未握手时不会触发 onSilence */
    @Test
    fun noTriggerWithoutHandshake() = runTest {
        val flow = MutableSharedFlow<Event>(replay = 1)
        var silenceTriggered = false
        val handler = createHandler(
            flow, baseSilenceTimeout = 5.minutes,
            onSilence = { silenceTriggered = true },
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        // 不发送握手事件，直接推进时间
        advanceTimeBy(10.minutes)

        assertFalse(silenceTriggered, "未握手时不应启动监控")
        handler.cancel()
    }

    /** 验证 cancel 后重置 currentTimeout */
    @Test
    fun cancelResetsTimeout() = runTest {
        val flow = MutableSharedFlow<Event>(replay = 1)
        val handler = createHandler(
            flow, baseSilenceTimeout = 5.minutes, maxSilenceTimeout = 40.minutes,
            onSilence = { },
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        // 触发一次退避
        flow.emit(json.decodeFromString(helloJson))
        advanceTimeBy(5.minutes + 1.seconds)
        assertEquals(10.minutes, handler.currentTimeout)

        handler.cancel()
        assertEquals(5.minutes, handler.currentTimeout, "cancel 应重置 currentTimeout")
    }

    /** 验证握手失败（code != 0）不启动监控 */
    @Test
    fun failedHandshakeDoesNotStartMonitoring() = runTest {
        val flow = MutableSharedFlow<Event>(replay = 1)
        var silenceTriggered = false
        val handler = createHandler(
            flow, baseSilenceTimeout = 5.minutes,
            onSilence = { silenceTriggered = true },
            dispatcher = UnconfinedTestDispatcher(testScheduler),
        )

        flow.emit(json.decodeFromString("""{"s":1,"d":{"code":40103}}"""))
        advanceTimeBy(10.minutes)

        assertFalse(silenceTriggered, "握手失败不应启动监控")
        handler.cancel()
    }
}
