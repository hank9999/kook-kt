package com.github.hank9999.kook.gateway

import com.github.hank9999.kook.gateway.entity.ResumeAckEvent
import com.github.hank9999.kook.gateway.handler.SequenceHandler
import com.github.hank9999.kook.gateway.handler.SequenceHandler.ProcessResult
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TestTimeSource

/** SequenceHandler 序列号处理器测试 */
class SequenceHandlerTest {

    /** 验证乱序到达的事件会被缓冲，待前序事件到达后一起按序输出 */
    @Test
    fun sequenceHandlerBuffersOutOfOrderEvents() = runTest {
        val handler = SequenceHandler()

        val outOfOrder = handler.process(ResumeAckEvent(2))
        assertIs<ProcessResult.Events>(outOfOrder)
        assertTrue(outOfOrder.events.isEmpty())

        val ordered = handler.process(ResumeAckEvent(1))
        assertIs<ProcessResult.Events>(ordered)
        assertEquals(listOf(1, 2), ordered.events.mapNotNull { it.sn })
        assertEquals(2, handler.value)
    }

    /** 验证重复序列号的事件被丢弃，返回空列表 */
    @Test
    fun sequenceHandlerDropsDuplicatedEvents() = runTest {
        val handler = SequenceHandler()
        handler.process(ResumeAckEvent(1))

        val duplicated = handler.process(ResumeAckEvent(1))
        assertIs<ProcessResult.Events>(duplicated)
        assertTrue(duplicated.events.isEmpty())
        assertEquals(1, handler.value)
    }

    /** 验证无序列号的事件直接透传，不影响序列号计数 */
    @Test
    fun sequenceHandlerPassesThroughEventsWithoutSn() = runTest {
        val handler = SequenceHandler()
        val result = handler.process(ResumeAckEvent(null))

        assertIs<ProcessResult.Events>(result)
        assertEquals(1, result.events.size)
        assertEquals(null, result.events.first().sn)
        assertEquals(0, handler.value)
    }

    /** 验证缓冲区超过上限时返回 BufferOverflow 信号 */
    @Test
    fun sequenceHandlerReturnsBufferOverflowWhenLimitExceeded() = runTest {
        val handler = SequenceHandler(bufferLimit = 3)

        // sn=2,3,4 乱序到达（缺 sn=1），buffer 逐渐填充
        for (sn in 2..4) {
            val result = handler.process(ResumeAckEvent(sn))
            assertIs<ProcessResult.Events>(result)
            assertTrue(result.events.isEmpty())
        }

        // sn=5 到达，buffer 大小超过 limit=3，触发溢出
        val overflow = handler.process(ResumeAckEvent(5))
        assertIs<ProcessResult.BufferOverflow>(overflow)
    }

    /** 验证缺号超时后返回 BufferTimeout 信号 */
    @Test
    fun sequenceHandlerReturnsBufferTimeoutWhenMissingSnNotReceived() = runTest {
        val timeSource = TestTimeSource()
        val handler = SequenceHandler(bufferTimeout = 10.seconds, timeSource = timeSource)

        // sn=2 到达但缺 sn=1，进入缓冲区，记录 bufferSince
        val buffered = handler.process(ResumeAckEvent(2))
        assertIs<ProcessResult.Events>(buffered)
        assertTrue(buffered.events.isEmpty())

        // 推进时间但未超时，仍正常缓冲
        timeSource += 9.seconds
        val stillBuffered = handler.process(ResumeAckEvent(3))
        assertIs<ProcessResult.Events>(stillBuffered)
        assertTrue(stillBuffered.events.isEmpty())

        // 推进时间超过 timeout，触发超时
        timeSource += 2.seconds
        val timeout = handler.process(ResumeAckEvent(4))
        assertIs<ProcessResult.BufferTimeout>(timeout)
    }

    /** 验证缺号补齐后 bufferSince 重置，不会误触发超时 */
    @Test
    fun sequenceHandlerResetsBufferSinceWhenGapFilled() = runTest {
        val timeSource = TestTimeSource()
        val handler = SequenceHandler(bufferTimeout = 10.seconds, timeSource = timeSource)

        // sn=2 缓冲
        handler.process(ResumeAckEvent(2))
        timeSource += 8.seconds

        // sn=1 到达，缺号补齐，释放 sn=1,2，bufferSince 应重置
        val filled = handler.process(ResumeAckEvent(1))
        assertIs<ProcessResult.Events>(filled)
        assertEquals(listOf(1, 2), filled.events.mapNotNull { it.sn })

        // 新的缺号出现（sn=4，缺 sn=3）
        timeSource += 8.seconds
        val newGap = handler.process(ResumeAckEvent(4))
        assertIs<ProcessResult.Events>(newGap)
        assertTrue(newGap.events.isEmpty())

        // 再过 8 秒（距新缺号仅 8 秒，未超过 10 秒），不应超时
        timeSource += 8.seconds
        val notTimeout = handler.process(ResumeAckEvent(5))
        assertIs<ProcessResult.Events>(notTimeout)
        assertTrue(notTimeout.events.isEmpty())
    }
}
